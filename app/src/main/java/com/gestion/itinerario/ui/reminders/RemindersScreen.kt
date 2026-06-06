package com.gestion.itinerario.ui.reminders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mantenimiento Periódico", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Primary40,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Default.NotificationAdd, null, tint = Color.White)
            }
        }
    ) { padding ->
        val bottomPad = maxOf(innerPadding.calculateBottomPadding(), padding.calculateBottomPadding())
        if (reminders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(
                    top = padding.calculateTopPadding(), bottom = bottomPad
                ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.NotificationsActive, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                        modifier = Modifier.size(64.dp))
                    Text("Sin recordatorios de mantenimiento",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Toca + para programar el próximo",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 8.dp, bottom = bottomPad + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reminders, key = { it.id }) { r ->
                    ReminderCard(r, onMarkDone = { viewModel.markDone(r) }, onDelete = { viewModel.delete(r) })
                }
            }
        }
    }
    if (showDialog) {
        ReminderFormDialog(onDismiss = { showDialog = false }, onSave = { viewModel.save(it); showDialog = false })
    }
}

@Composable
fun ReminderCard(r: MaintenanceReminder, onMarkDone: () -> Unit, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val now = System.currentTimeMillis()
    val isDue = r.nextServiceDate <= now
    val daysLeft = ((r.nextServiceDate - now) / 86_400_000L).toInt()
    val color = when {
        isDue -> StatusLowStock
        daysLeft <= 7 -> Color(0xFFFF8F00)
        else -> StatusCompleted
    }

    val intervalText = when (r.intervalUnit) {
        IntervalUnit.WEEKS  -> "Cada ${r.intervalValue} semana(s)"
        IntervalUnit.MONTHS -> "Cada ${r.intervalValue} mes(es)"
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDue -> StatusLowStock.copy(0.08f)
                daysLeft <= 7 -> Color(0xFFFF8F00).copy(0.06f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (r.equipmentId.isNotBlank())
                        Text("Equipo #${r.equipmentId}", fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                    Text(intervalText, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (r.notes.isNotBlank())
                        Text(r.notes, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (!isDue && daysLeft >= 0) {
                    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.15f)) {
                        Text("en $daysLeft d.",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = color,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Último servicio", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(if (r.lastServiceDate > 0L) sdf.format(Date(r.lastServiceDate)) else "Sin registro",
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Próximo servicio", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sdf.format(Date(r.nextServiceDate)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium, color = color)
                }
            }
            if (isDue) {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onMarkDone, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Marcar como realizado")
                }
            }
        }
    }
}

private data class IntervalOption(val value: Int, val unit: IntervalUnit, val label: String)

private val INTERVAL_OPTIONS = listOf(
    IntervalOption(1, IntervalUnit.WEEKS,  "1 semana"),
    IntervalOption(2, IntervalUnit.WEEKS,  "2 semanas"),
    IntervalOption(1, IntervalUnit.MONTHS, "1 mes"),
    IntervalOption(2, IntervalUnit.MONTHS, "2 meses"),
    IntervalOption(3, IntervalUnit.MONTHS, "3 meses"),
    IntervalOption(6, IntervalUnit.MONTHS, "6 meses")
)

@Composable
fun ReminderFormDialog(onDismiss: () -> Unit, onSave: (MaintenanceReminder) -> Unit) {
    var equipId  by remember { mutableStateOf("") }
    var notes    by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(INTERVAL_OPTIONS[4]) } // default 3 meses

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Recordatorio de Mantenimiento", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = equipId, onValueChange = { equipId = it },
                    label = { Text("ID / Nombre del equipo") },
                    leadingIcon = { Icon(Icons.Default.Engineering, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Próximo mantenimiento en:", style = MaterialTheme.typography.labelMedium)
                // Fila 1: semanas
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    INTERVAL_OPTIONS.filter { it.unit == IntervalUnit.WEEKS }.forEach { opt ->
                        FilterChip(
                            selected = selected == opt,
                            onClick = { selected = opt },
                            label = { Text(opt.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                // Fila 2: meses
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    INTERVAL_OPTIONS.filter { it.unit == IntervalUnit.MONTHS }.forEach { opt ->
                        FilterChip(
                            selected = selected == opt,
                            onClick = { selected = opt },
                            label = { Text(opt.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                // Fecha estimada del próximo mantenimiento
                val cal = Calendar.getInstance().apply {
                    when (selected.unit) {
                        IntervalUnit.WEEKS  -> add(Calendar.WEEK_OF_YEAR, selected.value)
                        IntervalUnit.MONTHS -> add(Calendar.MONTH, selected.value)
                    }
                }
                val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                Surface(shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Próximo: ${sdf.format(cal.time)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val cal = Calendar.getInstance()
                    when (selected.unit) {
                        IntervalUnit.WEEKS  -> cal.add(Calendar.WEEK_OF_YEAR, selected.value)
                        IntervalUnit.MONTHS -> cal.add(Calendar.MONTH, selected.value)
                    }
                    // intervalMonths: convert for backwards compat (approx)
                    val approxMonths = when (selected.unit) {
                        IntervalUnit.WEEKS  -> (selected.value / 4.0).coerceAtLeast(1.0).toInt()
                        IntervalUnit.MONTHS -> selected.value
                    }
                    onSave(MaintenanceReminder(
                        equipmentId = equipId.trim(),
                        intervalValue = selected.value,
                        intervalUnit = selected.unit,
                        intervalMonths = approxMonths,
                        lastServiceDate = now,
                        nextServiceDate = cal.timeInMillis,
                        notes = notes.trim()
                    ))
                },
                enabled = true
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
