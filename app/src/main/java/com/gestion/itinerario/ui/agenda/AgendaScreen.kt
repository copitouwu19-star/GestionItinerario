package com.gestion.itinerario.ui.agenda

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.ui.services.displayName
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// Azul = citas/appointments, Verde = mantenimientos
private val ColorCita = Color(0xFF1565C0)
private val ColorMantenimiento = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    innerPadding: PaddingValues,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Estado del calendario — siempre abre en el día de hoy
    val today = remember { Calendar.getInstance() }
    var currentMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }
    var currentYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var selectedDay by remember { mutableIntStateOf(today.get(Calendar.DAY_OF_MONTH)) }

    // Cuando la lista de citas cambia, nos aseguramos de que el día seleccionado siga siendo válido
    LaunchedEffect(appointments) {
        // Noop: el selectedDay ya apunta a hoy al inicio
    }

    // Citas del mes actual agrupadas por día
    val citasByDay = remember(appointments, currentMonth, currentYear) {
        appointments.groupBy { appt ->
            val cal = Calendar.getInstance().apply { timeInMillis = appt.dateTime }
            Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    // Citas del día seleccionado (usando filtro explícito por año/mes/día)
    val selectedDayCitas = remember(selectedDay, currentMonth, currentYear, appointments) {
        appointments.filter { appt ->
            val cal = Calendar.getInstance().apply { timeInMillis = appt.dateTime }
            cal.get(Calendar.YEAR) == currentYear &&
                    cal.get(Calendar.MONTH) == currentMonth &&
                    cal.get(Calendar.DAY_OF_MONTH) == selectedDay
        }.sortedBy { it.dateTime }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editAppointment = null; showDialog = true },
                containerColor = Primary40
            ) {
                Icon(Icons.Default.EventAvailable, null, tint = Color.White)
            }
        }
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = scaffoldPadding.calculateTopPadding(),
                    bottom = maxOf(
                        innerPadding.calculateBottomPadding(),
                        scaffoldPadding.calculateBottomPadding()
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Calendario mensual
            item {
                CalendarCard(
                    currentYear = currentYear,
                    currentMonth = currentMonth,
                    selectedDay = selectedDay,
                    citasByDay = citasByDay,
                    today = today,
                    onPreviousMonth = {
                        if (currentMonth == 0) { currentMonth = 11; currentYear-- }
                        else currentMonth--
                        selectedDay = 1
                    },
                    onNextMonth = {
                        if (currentMonth == 11) { currentMonth = 0; currentYear++ }
                        else currentMonth++
                        selectedDay = 1
                    },
                    onDaySelected = { selectedDay = it }
                )
            }

            // Leyenda de colores
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LegendItem(color = ColorCita, label = "Cita / Reparación")
                    LegendItem(color = ColorMantenimiento, label = "Mantenimiento")
                }
            }

            // Encabezado del día seleccionado
            item {
                val monthNames = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$selectedDay de ${monthNames[currentMonth]} $currentYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Primary80.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${selectedDayCitas.size} evento(s)",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary80
                        )
                    }
                }
            }

            // Lista de citas del día seleccionado
            if (selectedDayCitas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventBusy, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                "Sin eventos para este día",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(selectedDayCitas, key = { it.id }) { a ->
                    AppointmentCard(
                        a,
                        onEdit = { editAppointment = a; showDialog = true },
                        onCancel = { viewModel.cancel(a) },
                        onComplete = { viewModel.complete(a) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        AppointmentFormDialog(
            initial = editAppointment,
            onDismiss = { showDialog = false },
            onSave = { a ->
                if (editAppointment == null) viewModel.save(a)
                else viewModel.update(a.copy(id = editAppointment!!.id))
                showDialog = false
            }
        )
    }
}

// ─── Componente Calendario ────────────────────────────────────────────────────
@Composable
fun CalendarCard(
    currentYear: Int,
    currentMonth: Int,
    selectedDay: Int,
    citasByDay: Map<Triple<Int, Int, Int>, List<Appointment>>,
    today: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val monthNames = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val dayNames = arrayOf("L", "M", "X", "J", "V", "S", "D")

    // Calcular días del mes
    val firstDayCal = Calendar.getInstance().apply { set(currentYear, currentMonth, 1) }
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // Día de la semana del día 1 (Lunes = 0)
    var firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 2
    if (firstDayOfWeek < 0) firstDayOfWeek = 6

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header mes/año
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.ChevronLeft, null, tint = Primary80)
                }
                Text(
                    "${monthNames[currentMonth]} $currentYear",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, null, tint = Primary80)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Cabecera días de semana
            Row(modifier = Modifier.fillMaxWidth()) {
                dayNames.forEach { day ->
                    Text(
                        day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Cuadrícula de días
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
                            val dayAppointments = citasByDay[key] ?: emptyList()
                            val hasMantenimiento = dayAppointments.any { it.serviceType == ServiceType.MAINTENANCE }
                            val hasCita = dayAppointments.any { it.serviceType != ServiceType.MAINTENANCE }
                            val isToday = day == today.get(Calendar.DAY_OF_MONTH) &&
                                    currentMonth == today.get(Calendar.MONTH) &&
                                    currentYear == today.get(Calendar.YEAR)
                            val isSelected = day == selectedDay

                            CalendarDayCell(
                                day = day,
                                isToday = isToday,
                                isSelected = isSelected,
                                hasMantenimiento = hasMantenimiento,
                                hasCita = hasCita,
                                modifier = Modifier.weight(1f),
                                onClick = { onDaySelected(day) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasMantenimiento: Boolean,
    hasCita: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(
                if (isSelected) Modifier.background(Primary80)
                else if (isToday) Modifier.border(1.5.dp, Primary80, RoundedCornerShape(8.dp))
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
            // Indicadores de eventos
            if (hasMantenimiento || hasCita) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (hasCita) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else ColorCita)
                        )
                    }
                    if (hasMantenimiento) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White.copy(0.8f) else ColorMantenimiento)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Tarjeta de cita ──────────────────────────────────────────────────────────
@Composable
fun AppointmentCard(a: Appointment, onEdit: () -> Unit, onCancel: () -> Unit, onComplete: () -> Unit) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isMantenimiento = a.serviceType == ServiceType.MAINTENANCE
    val accentColor = if (isMantenimiento) ColorMantenimiento else ColorCita

    val statusLabel = when (a.status) {
        AppointmentStatus.SCHEDULED -> "Programada"
        AppointmentStatus.COMPLETED -> "Completada"
        AppointmentStatus.CANCELLED -> "Cancelada"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Barra lateral de color
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (isMantenimiento) Icons.Default.Build else Icons.Default.EventAvailable,
                        null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        sdf.format(Date(a.dateTime)),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            a.serviceType.displayName(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor
                        )
                    }
                }
                if (a.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        a.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (a.status) {
                        AppointmentStatus.SCHEDULED -> Primary80
                        AppointmentStatus.COMPLETED -> StatusCompleted
                        AppointmentStatus.CANCELLED -> StatusLowStock
                    }
                )
                if (a.status == AppointmentStatus.SCHEDULED) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onComplete,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("✓ Completar", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLowStock)
                        ) {
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

// ─── Formulario de cita ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFormDialog(initial: Appointment?, onDismiss: () -> Unit, onSave: (Appointment) -> Unit) {
    val context = LocalContext.current
    val calendar = remember {
        Calendar.getInstance().apply {
            initial?.let { timeInMillis = it.dateTime }
        }
    }
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var clientName by remember { mutableStateOf(initial?.notes?.substringBefore(" —") ?: "") }
    var dateStr by remember { mutableStateOf(initial?.let { sdf.format(Date(it.dateTime)) } ?: "") }
    var type by remember { mutableStateOf(initial?.serviceType ?: ServiceType.MAINTENANCE) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    val timePicker = TimePickerDialog(
        context,
        { _, h, min ->
            calendar.set(Calendar.HOUR_OF_DAY, h)
            calendar.set(Calendar.MINUTE, min)
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
            Text(
                if (initial == null) "Nueva Cita" else "Editar Cita",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Nombre del cliente *") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = {},
                    label = { Text("Fecha y hora *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.EditCalendar, null, tint = Primary80)
                        }
                    }
                )
                Text("Tipo:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ServiceType.values().forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.displayName(), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        Appointment(
                            clientId = initial?.clientId ?: 0L,
                            dateTime = calendar.timeInMillis,
                            serviceType = type,
                            notes = if (clientName.isNotBlank()) "$clientName — $notes" else notes
                        )
                    )
                },
                enabled = clientName.isNotBlank() && dateStr.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
