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
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: ReminderViewModel = hiltViewModel()) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Recordatorios de Mantenimiento", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = Primary40) {
                Icon(Icons.Default.NotificationAdd, null, tint = Color.White)
            }
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(64.dp))
                    Text("Sin recordatorios activos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
    val isDue = r.nextServiceDate <= System.currentTimeMillis()
    val color = if (isDue) StatusLowStock else StatusCompleted

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDue) StatusLowStock.copy(0.08f) else MaterialTheme.colorScheme.surfaceVariant
        )) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Engineering, null, tint = color, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Equipo #${r.equipmentId}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Cada ${r.intervalMonths} mes(es)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
            }
            Divider(color = MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("Último servicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sdf.format(Date(r.lastServiceDate)), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Próximo servicio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(sdf.format(Date(r.nextServiceDate)), style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium, color = color)
                }
            }
            if (isDue) {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onMarkDone, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Marcar como realizado")
                }
            }
        }
    }
}

@Composable
fun ReminderFormDialog(onDismiss: () -> Unit, onSave: (MaintenanceReminder) -> Unit) {
    var equipId by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("3") }
    val options = listOf("1", "3", "6", "12")

    AlertDialog(onDismissRequest = onDismiss,
        title = { Text("Nuevo Recordatorio", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = equipId, onValueChange = { equipId = it }, label = { Text("ID Equipo") }, modifier = Modifier.fillMaxWidth())
                Text("Intervalo de mantenimiento:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { opt ->
                        FilterChip(selected = interval == opt, onClick = { interval = opt },
                            label = { Text("${opt}m") })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = Calendar.getInstance()
                val last = cal.timeInMillis
                cal.add(Calendar.MONTH, interval.toIntOrNull() ?: 3)
                val next = cal.timeInMillis
                onSave(MaintenanceReminder(equipmentId = equipId.toLongOrNull() ?: 0L,
                    intervalMonths = interval.toIntOrNull() ?: 3, lastServiceDate = last, nextServiceDate = next))
            }, enabled = equipId.isNotBlank()) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
