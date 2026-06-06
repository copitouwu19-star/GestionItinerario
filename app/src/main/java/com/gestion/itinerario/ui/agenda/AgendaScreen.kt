package com.gestion.itinerario.ui.agenda

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.ui.services.displayName
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

private val ColorCita = Color(0xFF1565C0)
private val ColorMantenimiento = Color(0xFF2E7D32)

// Tipos de equipo disponibles
val EQUIPMENT_TYPES = listOf("Nevera", "Aire Acondicionado", "Lavadora", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    innerPadding: PaddingValues,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val clients      by viewModel.clients.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Diálogo de confirmación para cancelar
    var showCancelConfirm by remember { mutableStateOf(false) }
    var appointmentToCancel by remember { mutableStateOf<Appointment?>(null) }

    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.needsExactAlarmPermission.collect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                )
                context.startActivity(intent)
            }
        }
    }

    val today = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var currentYear  by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay  by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    val citasByDay = remember(appointments, currentMonth, currentYear) {
        appointments.groupBy { appt ->
            val cal = Calendar.getInstance().apply { timeInMillis = appt.dateTime }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    val selectedDayCitas = remember(selectedDay, currentMonth, currentYear, appointments) {
        appointments.filter { appt ->
            val cal = Calendar.getInstance().apply { timeInMillis = appt.dateTime }
            cal.get(Calendar.YEAR) == currentYear &&
                    cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }.sortedBy { it.dateTime }
    }

    // Diálogo de confirmación cancelar cita
    if (showCancelConfirm && appointmentToCancel != null) {
        AlertDialog(
            onDismissRequest = { showCancelConfirm = false; appointmentToCancel = null },
            icon  = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF8F00)) },
            title = { Text("Cancelar cita", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Estás segura de que deseas cancelar esta cita? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        appointmentToCancel?.let { viewModel.cancel(it) }
                        showCancelConfirm = false; appointmentToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusLowStock)
                ) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirm = false; appointmentToCancel = null }) {
                    Text("No, volver")
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agenda", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editAppointment = null; showDialog = true },
                containerColor = Primary40
            ) { Icon(Icons.Default.EventAvailable, null, tint = Color.White) }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(
                top    = scaffoldPadding.calculateTopPadding(),
                bottom = maxOf(innerPadding.calculateBottomPadding(), scaffoldPadding.calculateBottomPadding())
            ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarCard(
                    currentYear = currentYear, currentMonth = currentMonth,
                    selectedDay = selectedDay, citasByDay = citasByDay, today = today,
                    onPreviousMonth = {
                        if (currentMonth == 0) { currentMonth = 11; currentYear-- } else currentMonth--
                        selectedDay = 1
                    },
                    onNextMonth = {
                        if (currentMonth == 11) { currentMonth = 0; currentYear++ } else currentMonth++
                        selectedDay = 1
                    },
                    onDaySelected = { selectedDay = it }
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendItem(color = ColorCita, label = "Cita / Reparación")
                    LegendItem(color = ColorMantenimiento, label = "Mantenimiento")
                }
            }
            item {
                val monthNames = arrayOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
                    "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("$selectedDay de ${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground)
                    Surface(shape = RoundedCornerShape(20.dp), color = Primary80.copy(alpha = 0.15f)) {
                        Text("${selectedDayCitas.size} evento(s)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, color = Primary80)
                    }
                }
            }
            if (selectedDayCitas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                modifier = Modifier.size(36.dp))
                            Text("Sin eventos para este día",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                items(selectedDayCitas, key = { it.id }) { a ->
                    AppointmentCard(
                        a,
                        onEdit     = { editAppointment = a; showDialog = true },
                        onCancel   = { appointmentToCancel = a; showCancelConfirm = true },
                        onStart    = { viewModel.startAppointment(a) },
                        onComplete = { viewModel.complete(a) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AppointmentFormDialog(
            initial   = editAppointment,
            clients   = clients,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onSave    = { a ->
                if (editAppointment == null) viewModel.save(a)
                else viewModel.update(a.copy(id = editAppointment!!.id))
                showDialog = false
            }
        )
    }
}

// ─── Calendario ───────────────────────────────────────────────────────────────
@Composable
fun CalendarCard(
    currentYear: Int, currentMonth: Int, selectedDay: Int,
    citasByDay: Map<Triple<Int,Int,Int>, List<Appointment>>,
    today: Calendar,
    onPreviousMonth: () -> Unit, onNextMonth: () -> Unit, onDaySelected: (Int) -> Unit
) {
    val monthNames = arrayOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
        "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
    val dayNames = arrayOf("L","M","X","J","V","S","D")
    val firstDayCal = Calendar.getInstance().apply { set(currentYear, currentMonth, 1) }
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    var firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek = 6

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousMonth) { Icon(Icons.Default.ChevronLeft, null, tint = Primary80) }
                Text("${monthNames[currentMonth]} $currentYear", fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null, tint = Primary80) }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(4.dp))
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        if (day < 1 || day > daysInMonth) {
                            Box(modifier = Modifier.weight(1f).height(42.dp))
                        } else {
                            val key = Triple(currentYear, currentMonth, day)
                            val dayApps = citasByDay[key] ?: emptyList()
                            val hasMantenimiento = dayApps.any { it.serviceType == ServiceType.MAINTENANCE }
                            val hasCita = dayApps.any { it.serviceType != ServiceType.MAINTENANCE }
                            val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                                    currentMonth == today.get(Calendar.MONTH) &&
                                    currentYear == today.get(Calendar.YEAR)
                            CalendarDayCell(day, isToday, day == selectedDay, hasMantenimiento, hasCita,
                                Modifier.weight(1f)) { onDaySelected(day) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int, isToday: Boolean, isSelected: Boolean,
    hasMantenimiento: Boolean, hasCita: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit
) {
    Box(modifier = modifier.height(42.dp).padding(2.dp).clip(RoundedCornerShape(8.dp))
        .then(if (isSelected) Modifier.background(Primary80)
              else if (isToday) Modifier.border(1.5.dp, Primary80, RoundedCornerShape(8.dp))
              else Modifier)
        .clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(day.toString(), fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
            if (hasMantenimiento || hasCita) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (hasCita) Box(modifier = Modifier.size(5.dp).clip(CircleShape)
                        .background(if (isSelected) Color.White else ColorCita))
                    if (hasMantenimiento) Box(modifier = Modifier.size(5.dp).clip(CircleShape)
                        .background(if (isSelected) Color.White.copy(0.8f) else ColorMantenimiento))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Tarjeta de cita (con ciclo SCHEDULED → IN_PROGRESS → COMPLETED) ──────────
@Composable
fun AppointmentCard(
    a: Appointment,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isMantenimiento = a.serviceType == ServiceType.MAINTENANCE
    val accentColor = if (isMantenimiento) ColorMantenimiento else ColorCita

    val (statusLabel, statusColor) = when (a.status) {
        AppointmentStatus.SCHEDULED   -> "Programada"  to Primary80
        AppointmentStatus.IN_PROGRESS -> "En Proceso"  to StatusInRepair
        AppointmentStatus.COMPLETED   -> "Completada"  to StatusCompleted
        AppointmentStatus.CANCELLED   -> "Cancelada"   to StatusLowStock
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                .background(accentColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(if (isMantenimiento) Icons.Default.Build else Icons.Default.EventAvailable,
                        null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Text(sdf.format(Date(a.dateTime)), fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = accentColor.copy(alpha = 0.15f)) {
                        Text(a.serviceType.displayName(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, color = accentColor)
                    }
                }
                if (a.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(a.notes, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Tipo de equipo
                if (a.equipmentType.isNotBlank()) {
                    Text("🔧 ${a.equipmentType}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)

                // Botones de acción según ciclo de estado
                if (a.status == AppointmentStatus.SCHEDULED || a.status == AppointmentStatus.IN_PROGRESS) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (a.status) {
                            AppointmentStatus.SCHEDULED -> {
                                OutlinedButton(onClick = onStart,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp)) {
                                    Text("→ En Proceso", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            AppointmentStatus.IN_PROGRESS -> {
                                OutlinedButton(onClick = onComplete,
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCompleted)) {
                                    Text("✓ Completar", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            else -> {}
                        }
                        OutlinedButton(onClick = onCancel,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLowStock)) {
                            Text("✕ Cancelar", style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Formulario de cita ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormDialog(
    initial: Appointment?,
    clients: List<Client>,
    viewModel: AgendaViewModel,
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance().apply { initial?.let { timeInMillis = it.dateTime } } }
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var dateStr by remember { mutableStateOf(initial?.let { sdf.format(Date(it.dateTime)) } ?: "") }
    var type    by remember { mutableStateOf(initial?.serviceType ?: ServiceType.MAINTENANCE) }
    var notes   by remember { mutableStateOf(initial?.notes ?: "") }
    var equipmentType by remember { mutableStateOf(initial?.equipmentType ?: "") }

    val initialClient = remember(initial, clients) { clients.find { it.id == initial?.clientId } }
    var selectedClient by remember { mutableStateOf<Client?>(initialClient) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var conflictAppointment by remember { mutableStateOf<Appointment?>(null) }
    var pendingSave         by remember { mutableStateOf<Appointment?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val timePicker = TimePickerDialog(context, { _, h, min ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
        dateStr = sdf.format(calendar.time)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d); timePicker.show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    if (conflictAppointment != null && pendingSave != null) {
        val conflict = conflictAppointment!!
        val cSdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        AlertDialog(
            onDismissRequest = { conflictAppointment = null; pendingSave = null },
            icon  = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF8F00)) },
            title = { Text("Conflicto de horario", fontWeight = FontWeight.Bold) },
            text  = { Text("Ya tienes una cita el ${cSdf.format(Date(conflict.dateTime))}.\n\n¿Deseas agendar de todas formas?") },
            confirmButton = { Button(onClick = { onSave(pendingSave!!); conflictAppointment = null; pendingSave = null }) { Text("Sí, agendar igual") } },
            dismissButton = { TextButton(onClick = { conflictAppointment = null; pendingSave = null }) { Text("No, cambiar hora") } }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nueva Cita" else "Editar Cita", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Dropdown clientes
                ExposedDropdownMenuBox(expanded = clientDropdownExpanded,
                    onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }) {
                    OutlinedTextField(
                        value = selectedClient?.let {
                            "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}"
                        } ?: "",
                        onValueChange = {}, readOnly = true, label = { Text("Cliente *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false }) {
                        if (clients.isEmpty()) {
                            DropdownMenuItem(text = { Text("Sin clientes registrados") },
                                onClick = { clientDropdownExpanded = false })
                        } else {
                            clients.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text("${client.name} ${client.lastName}".trim()) },
                                    onClick = { selectedClient = client; clientDropdownExpanded = false },
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary80) }
                                )
                            }
                        }
                    }
                }
                if (clients.isEmpty()) {
                    Text("⚠️ No hay clientes registrados.", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
                // Fecha y hora
                OutlinedTextField(value = dateStr, onValueChange = {},
                    label = { Text("Fecha y hora *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(), readOnly = true,
                    trailingIcon = { IconButton(onClick = { datePicker.show() }) {
                        Icon(Icons.Default.EditCalendar, null, tint = Primary80) } })
                // Motivo
                Text("Motivo:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServiceType.values().forEach { st ->
                        FilterChip(selected = type == st, onClick = { type = st },
                            label = { Text(when (st) {
                                ServiceType.MAINTENANCE  -> "Mant."
                                ServiceType.REPAIR       -> "Reparación"
                                ServiceType.INSTALLATION -> "Instalación"
                            }, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                // Tipo de equipo
                Text("Tipo de equipo:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    EQUIPMENT_TYPES.take(2).forEach { et ->
                        FilterChip(selected = equipmentType == et, onClick = { equipmentType = if (equipmentType == et) "" else et },
                            modifier = Modifier.weight(1f),
                            label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    EQUIPMENT_TYPES.drop(2).forEach { et ->
                        FilterChip(selected = equipmentType == et, onClick = { equipmentType = if (equipmentType == et) "" else et },
                            modifier = Modifier.weight(1f),
                            label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                // Notas
                OutlinedTextField(value = notes, onValueChange = { notes = it },
                    label = { Text("Notas adicionales") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val client = selectedClient ?: return@Button
                    val clientDisplayName = "${client.name}${if (client.lastName.isNotBlank()) " ${client.lastName}" else ""}"
                    val newAppointment = Appointment(
                        clientId      = client.id,
                        dateTime      = calendar.timeInMillis,
                        serviceType   = type,
                        equipmentType = equipmentType,
                        notes         = if (notes.isNotBlank()) "$clientDisplayName — $notes" else clientDisplayName
                    )
                    coroutineScope.launch {
                        val excludeId = initial?.id ?: ""
                        val conflict  = viewModel.checkConflict(calendar.timeInMillis, excludeId)
                        if (conflict != null) { conflictAppointment = conflict; pendingSave = newAppointment }
                        else onSave(newAppointment)
                    }
                },
                enabled = selectedClient != null && dateStr.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
