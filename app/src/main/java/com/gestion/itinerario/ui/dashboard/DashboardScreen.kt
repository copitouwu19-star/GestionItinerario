package com.gestion.itinerario.ui.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.*
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Tipos de equipo del negocio
enum class EquipmentType(val label: String) {
    NEVERA("Nevera"),
    AIRE("Aire Acondicionado"),
    LAVADORA("Lavadora"),
    OTRO("Otro")
}

@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val inRepair by viewModel.inRepairCount.collectAsStateWithLifecycle()
    val todayCitas by viewModel.todayAppointments.collectAsStateWithLifecycle()
    val lowStock by viewModel.lowStockCount.collectAsStateWithLifecycle()
    val activeServices by viewModel.activeServices.collectAsStateWithLifecycle()
    val totalServices by viewModel.totalServices.collectAsStateWithLifecycle()

    // Diálogos de acciones rápidas
    var showNewOrderDialog by remember { mutableStateOf(false) }
    var showNewCitaDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Panel de Control",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Visión general del negocio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.Business, contentDescription = null,
                tint = Primary80, modifier = Modifier.size(32.dp))
        }

        // Stats cards row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "En Reparación",
                value = inRepair.toString(),
                icon = Icons.Default.Build,
                gradientColors = listOf(Color(0xFF1E3A5F), Color(0xFF2196F3)),
                valueColor = StatusDelivered
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Citas Hoy",
                value = todayCitas.toString(),
                icon = Icons.Default.CalendarToday,
                gradientColors = listOf(Color(0xFF1A237E), Color(0xFF7C4DFF)),
                valueColor = Primary80
            )
        }

        // Stats cards row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Stock Bajo",
                value = lowStock.toString(),
                icon = Icons.Default.Warning,
                gradientColors = listOf(Color(0xFF3E0000), Color(0xFFB71C1C)),
                valueColor = StatusLowStock
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Servicios Activos",
                value = activeServices.toString(),
                icon = Icons.Default.Engineering,
                gradientColors = listOf(Color(0xFF1B5E20), Color(0xFF43A047)),
                valueColor = StatusCompleted
            )
        }

        // Summary card
        SummaryCard(totalServices = totalServices)

        // Quick actions
        Text(
            "Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionButton(Modifier.weight(1f), "Nueva Orden", Icons.Default.AddCircle, Primary80) {
                showNewOrderDialog = true
            }
            QuickActionButton(Modifier.weight(1f), "Nueva Cita", Icons.Default.EventAvailable, Secondary80) {
                showNewCitaDialog = true
            }
            QuickActionButton(Modifier.weight(1f), "Inventario", Icons.Default.Inventory, Tertiary80) {
                onNavigate("inventory") // la navegación correcta se maneja en MainActivity con popUpTo
            }
        }
    }

    // Diálogo Nueva Orden de Servicio
    if (showNewOrderDialog) {
        NewServiceOrderDialog(
            onDismiss = { showNewOrderDialog = false },
            onSave = { order ->
                viewModel.saveOrder(order)
                showNewOrderDialog = false
            }
        )
    }

    // Diálogo Nueva Cita
    if (showNewCitaDialog) {
        NewAppointmentDialog(
            onDismiss = { showNewCitaDialog = false },
            onSave = { appointment ->
                viewModel.saveAppointment(appointment)
                showNewCitaDialog = false
            }
        )
    }
}

// ─── Diálogo: Nueva Orden de Servicio ────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewServiceOrderDialog(onDismiss: () -> Unit, onSave: (ServiceOrder) -> Unit) {
    var clientName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.MAINTENANCE) }
    var equipType by remember { mutableStateOf(EquipmentType.NEVERA) }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dateStr by remember { mutableStateOf("") }

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            dateStr = "$d/${m + 1}/$y"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCircle, null, tint = Primary80)
                Text("Nueva Orden de Servicio", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cliente
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del cliente *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Fecha
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text("Fecha *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.EditCalendar, null, tint = Primary80)
                        }
                    }
                )

                // Tipo de equipo
                Text("Tipo de equipo:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EquipmentType.values().take(2).forEach { et ->
                            FilterChip(
                                selected = equipType == et,
                                onClick = { equipType = et },
                                label = { Text(et.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EquipmentType.values().drop(2).forEach { et ->
                            FilterChip(
                                selected = equipType == et,
                                onClick = { equipType = et },
                                label = { Text(et.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Tipo de servicio
                Text("Tipo de mantenimiento:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServiceType.values().forEach { st ->
                        FilterChip(
                            selected = serviceType == st,
                            onClick = { serviceType = st },
                            label = {
                                Text(
                                    when (st) {
                                        ServiceType.MAINTENANCE -> "Mant."
                                        ServiceType.REPAIR -> "Reparación"
                                        ServiceType.INSTALLATION -> "Instalación"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Descripción (opcional)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción del problema (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ServiceOrder(
                            clientId = 0L,
                            equipmentId = 0L,
                            type = serviceType,
                            description = "${equipType.label} — $clientName" +
                                if (description.isNotBlank()) ": $description" else "",
                            diagnosis = "",
                            createdAt = if (dateStr.isNotBlank()) calendar.timeInMillis else System.currentTimeMillis()
                        )
                    )
                },
                enabled = clientName.isNotBlank() && dateStr.isNotBlank() // descripción es opcional
            ) { Text("Guardar Orden") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ─── Diálogo: Nueva Cita ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(onDismiss: () -> Unit, onSave: (Appointment) -> Unit) {
    var clientName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var serviceType by remember { mutableStateOf(ServiceType.MAINTENANCE) }
    var equipType by remember { mutableStateOf(EquipmentType.NEVERA) }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dateStr by remember { mutableStateOf("") }

    val timePicker = TimePickerDialog(
        context,
        { _, h, min ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, min)
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateStr = sdf.format(calendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val datePicker = DatePickerDialog(
        context,
        { _, y, m, d ->
            calendar.set(y, m, d)
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EventAvailable, null, tint = Secondary80)
                Text("Nueva Cita", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Cliente
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del cliente *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Fecha y hora
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text("Fecha y hora *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.EditCalendar, null, tint = Secondary80)
                        }
                    }
                )

                // Tipo de equipo
                Text("Tipo de equipo:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EquipmentType.values().take(2).forEach { et ->
                            FilterChip(
                                selected = equipType == et,
                                onClick = { equipType = et },
                                label = { Text(et.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EquipmentType.values().drop(2).forEach { et ->
                            FilterChip(
                                selected = equipType == et,
                                onClick = { equipType = et },
                                label = { Text(et.label, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Tipo de servicio (cita vs mantenimiento)
                Text("Motivo de la cita:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServiceType.values().forEach { st ->
                        FilterChip(
                            selected = serviceType == st,
                            onClick = { serviceType = st },
                            label = {
                                Text(
                                    when (st) {
                                        ServiceType.MAINTENANCE -> "Mant."
                                        ServiceType.REPAIR -> "Reparación"
                                        ServiceType.INSTALLATION -> "Instalación"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }

                // Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Appointment(
                            clientId = 0L,
                            dateTime = calendar.timeInMillis,
                            serviceType = serviceType,
                            notes = "${equipType.label} — $clientName. $notes"
                        )
                    )
                },
                enabled = clientName.isNotBlank() && dateStr.isNotBlank()
            ) { Text("Guardar Cita") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

// ─── Componentes ─────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    valueColor: Color
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = valueColor.copy(alpha = alpha))
            Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun SummaryCard(totalServices: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total de Servicios", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(totalServices.toString(), style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text("realizados hasta hoy", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.BarChart, contentDescription = null,
                tint = Primary80, modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun QuickActionButton(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ModuleCard(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = color,
                textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
        }
    }
}
