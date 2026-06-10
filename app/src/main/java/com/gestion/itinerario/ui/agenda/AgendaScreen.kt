package com.gestion.itinerario.ui.agenda

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.painterResource
import com.gestion.itinerario.R
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.ui.services.displayName
import com.gestion.itinerario.ui.theme.*
import com.gestion.itinerario.util.hoursUntil
import com.gestion.itinerario.util.sendAppointmentReminderViaWhatsApp
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch

private val WhatsAppGreen = Color(0xFF25D366)

private val ColorCita = Color(0xFF1565C0)
private val ColorMantenimiento = Color(0xFF2E7D32)

// Tipos de equipo disponibles
val EQUIPMENT_TYPES = listOf("Nevera", "Aire Acondicionado", "Lavadora", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    innerPadding: PaddingValues,
    onNavigateToProfile: () -> Unit = {},
    viewModel: AgendaViewModel = hiltViewModel(),
    profileViewModel: com.gestion.itinerario.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val appointments by viewModel.appointments.collectAsStateWithLifecycle()
    val clients      by viewModel.clients.collectAsStateWithLifecycle()
    val companyProfile by profileViewModel.profile.collectAsStateWithLifecycle()
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

    val routeStops = remember(selectedDayCitas, clients) {
        selectedDayCitas
            .filter { it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.IN_PROGRESS }
            .mapNotNull { appt -> clients.firstOrNull { c -> c.id == appt.clientId }?.address?.takeIf { it.isNotBlank() } }
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
        topBar = {
            TopAppBar(
                title = { Text("Agenda", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil",
                            tint = com.gestion.itinerario.ui.theme.Primary80,
                            modifier = Modifier.size(28.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editAppointment = null; showDialog = true },
                containerColor = Primary40,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
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
            if (routeStops.isNotEmpty()) {
                item {
                    OutlinedButton(
                        onClick = {
                            val destination = Uri.encode(routeStops.last())
                            val uri = if (routeStops.size > 1) {
                                val waypoints = "optimize:true|" + routeStops.dropLast(1).joinToString("|") { Uri.encode(it) }
                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination&waypoints=$waypoints&travelmode=driving")
                            } else {
                                Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destination&travelmode=driving")
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ver ruta optimizada del día (${routeStops.size} parada${if (routeStops.size != 1) "s" else ""})")
                    }
                }
            }
            if (selectedDayCitas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            val pulse = rememberInfiniteTransition(label = "crearCitaPulse")
                            val pulseScale by pulse.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.12f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseScale"
                            )
                            val pulseAlpha by pulse.animateFloat(
                                initialValue = 0.55f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulseAlpha"
                            )
                            Box(modifier = Modifier
                                .size(72.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.EventAvailable, null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                                    modifier = Modifier.size(34.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("Sin eventos para este día",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium)
                            Text("¡Es un buen momento para organizar tus clientes!",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            val gradient = Brush.linearGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                            )
                            Surface(
                                onClick = { editAppointment = null; showDialog = true },
                                modifier = Modifier.scale(pulseScale),
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.background(gradient).padding(horizontal = 28.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("CREAR CITA", color = Color.White, fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            } else {
                items(selectedDayCitas, key = { it.id }) { a ->
                    AppointmentCard(
                        a,
                        client      = clients.firstOrNull { it.id == a.clientId },
                        companyName = companyProfile.companyName,
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
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
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
    val gradient = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
    Box(modifier = modifier.height(42.dp).padding(2.dp).clip(RoundedCornerShape(8.dp))
        .then(if (isSelected) Modifier.background(Primary80) else Modifier)
        .clickable { onClick() }, contentAlignment = Alignment.Center) {
        if (isToday && !isSelected) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(gradient))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(day.toString(), fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected || isToday) Color.White else MaterialTheme.colorScheme.onSurface)
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
    client: Client?,
    companyName: String,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isMantenimiento = a.serviceType == ServiceType.MAINTENANCE
    val accentColor = if (isMantenimiento) ColorMantenimiento else ColorCita

    val (statusLabel, statusColor) = when (a.status) {
        AppointmentStatus.SCHEDULED   -> "Programada"  to Primary80
        AppointmentStatus.IN_PROGRESS -> "En Proceso"  to StatusInRepair
        AppointmentStatus.COMPLETED   -> "Completada"  to StatusCompleted
        AppointmentStatus.CANCELLED   -> "Cancelada"   to StatusLowStock
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
        Box {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                .background(accentColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)))
            Column(modifier = Modifier.padding(12.dp).padding(end = 36.dp).weight(1f)) {
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
                if (a.status == AppointmentStatus.CANCELLED) {
                    Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
                } else {
                    Spacer(Modifier.height(6.dp))
                    AppointmentStatusStepper(status = a.status)
                }

                // Aviso de recordatorio próximo (24h / 1h antes de la cita)
                if (a.status == AppointmentStatus.SCHEDULED && client?.phone?.isNotBlank() == true) {
                    val hoursLeft = hoursUntil(a)
                    if (hoursLeft in 0.0..24.0) {
                        Spacer(Modifier.height(4.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = WhatsAppGreen.copy(alpha = 0.12f)) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(painterResource(R.drawable.ic_whatsapp), null,
                                    tint = WhatsAppGreen, modifier = Modifier.size(14.dp))
                                Text(
                                    if (hoursLeft <= 1.0) "Cita en menos de 1 hora — recuérdale al cliente"
                                    else "Cita en menos de 24 horas — recuérdale al cliente",
                                    style = MaterialTheme.typography.labelSmall, color = WhatsAppGreen
                                )
                            }
                        }
                    }
                }

                // Botones de acción según ciclo de estado
                if (a.status == AppointmentStatus.SCHEDULED || a.status == AppointmentStatus.IN_PROGRESS) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        when (a.status) {
                            AppointmentStatus.SCHEDULED -> {
                                GradientPillButton(
                                    label = "EN PROCESO",
                                    gradient = Brush.linearGradient(
                                        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                    ),
                                    onClick = onStart
                                )
                            }
                            AppointmentStatus.IN_PROGRESS -> {
                                GradientPillButton(
                                    label = "COMPLETAR",
                                    gradient = Brush.linearGradient(listOf(StatusCompleted, MaterialTheme.colorScheme.tertiary)),
                                    onClick = onComplete
                                )
                            }
                            else -> {}
                        }
                        Spacer(Modifier.weight(1f))
                        if (a.status == AppointmentStatus.SCHEDULED && client?.phone?.isNotBlank() == true) {
                            IconButton(
                                onClick = { sendAppointmentReminderViaWhatsApp(context, client, a, companyName) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(painterResource(R.drawable.ic_whatsapp), "Recordar por WhatsApp",
                                    tint = WhatsAppGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
            // Ícono de papelera en la esquina superior derecha (reemplaza el botón "CANCELAR")
            if (a.status == AppointmentStatus.SCHEDULED || a.status == AppointmentStatus.IN_PROGRESS) {
                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(30.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Cancelar cita",
                        tint = StatusLowStock, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

/** Mini-stepper Pendiente → En Proceso → Finalizado: el paso actual se resalta con el degradado de la app. */
@Composable
private fun AppointmentStatusStepper(status: AppointmentStatus) {
    val steps = listOf("Pendiente", "En Proceso", "Finalizado")
    val currentIndex = when (status) {
        AppointmentStatus.SCHEDULED   -> 0
        AppointmentStatus.IN_PROGRESS -> 1
        AppointmentStatus.COMPLETED   -> 2
        AppointmentStatus.CANCELLED   -> 0
    }
    val gradient = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))

    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        steps.forEachIndexed { index, label ->
            val isCurrent = index == currentIndex
            val isDone = index < currentIndex

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .then(
                            when {
                                isCurrent -> Modifier.background(gradient)
                                isDone    -> Modifier.background(StatusCompleted)
                                else      -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    } else {
                        Text((index + 1).toString(),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(label, style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isCurrent -> MaterialTheme.colorScheme.primary
                        isDone    -> StatusCompleted
                        else      -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center)
            }
            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.weight(0.5f).padding(top = 10.dp),
                    thickness = 2.dp,
                    color = if (index < currentIndex) StatusCompleted else MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

/** Botón "píldora" con relleno en degradado, usado para acciones destacadas de cambio de estado. */
@Composable
private fun GradientPillButton(label: String, gradient: Brush, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(50), color = Color.Transparent) {
        Box(modifier = Modifier.background(gradient).padding(horizontal = 16.dp, vertical = 7.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
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
    val dateSdf  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var dateStr  by remember { mutableStateOf(initial?.let { dateSdf.format(Date(it.dateTime)) } ?: "") }
    var timeStr  by remember { mutableStateOf(initial?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it.dateTime)) } ?: "") }
    var type     by remember { mutableStateOf(initial?.serviceType ?: ServiceType.MAINTENANCE) }
    var notes    by remember { mutableStateOf(initial?.notes ?: "") }
    var equipmentType by remember { mutableStateOf(initial?.equipmentType ?: "") }

    val initialClient = remember(initial, clients) { clients.find { it.id == initial?.clientId } }
    var selectedClient         by remember { mutableStateOf<Client?>(initialClient) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var conflictAppointment    by remember { mutableStateOf<Appointment?>(null) }
    var pendingSave            by remember { mutableStateOf<Appointment?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val serviceTypes  = listOf(ServiceType.MAINTENANCE, ServiceType.REPAIR, ServiceType.INSTALLATION)
    val serviceIcons  = mapOf(
        ServiceType.MAINTENANCE  to Icons.Default.Build,
        ServiceType.REPAIR       to Icons.Default.Settings,
        ServiceType.INSTALLATION to Icons.Default.HomeRepairService
    )
    val serviceLabels = mapOf(
        ServiceType.MAINTENANCE  to "Mant.",
        ServiceType.REPAIR       to "Reparación",
        ServiceType.INSTALLATION to "Instalación"
    )
    val equipmentIcons = mapOf(
        "Nevera"             to Icons.Default.AcUnit,
        "Aire Acondicionado" to Icons.Default.Air,
        "Lavadora"           to Icons.Default.WaterDrop,
        "Otro"               to Icons.Default.MoreHoriz
    )

    val timePicker = TimePickerDialog(context, { _, h, min ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
        timeStr = String.format("%02d:%02d", h, min)
        dateStr = dateSdf.format(calendar.time)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        dateStr = String.format("%02d/%02d/%04d", d, m + 1, y)
        timePicker.show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    // Diálogo de conflicto (overlay sobre el formulario)
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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(if (initial == null) "Nueva Cita" else "Editar Cita", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Cancelar")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                },
                bottomBar = {
                    Surface(shadowElevation = 8.dp, color = Color.White) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50)
                            ) { Text("Cancelar") }
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
                                enabled = selectedClient != null && dateStr.isNotBlank() && timeStr.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.linearGradient(listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null,
                                            tint = Color.White, modifier = Modifier.size(18.dp))
                                        Text("Programar", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ── Banner ────────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary))
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.EventAvailable, null,
                                        tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(if (initial == null) "Agendar Servicio" else "Editar Cita",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    Text("Complete los detalles para la visita técnica.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // ── Cliente ───────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("CLIENTE *", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { clientDropdownExpanded = !clientDropdownExpanded },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Person, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp))
                                    Text(
                                        selectedClient?.let {
                                            "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}"
                                        } ?: "Seleccione un cliente",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (selectedClient != null)
                                            MaterialTheme.colorScheme.onSurface
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp))
                                }
                            }
                            DropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (clients.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Sin clientes — ve a Clientes y agrega uno",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        onClick = { clientDropdownExpanded = false })
                                } else {
                                    clients.forEach { client ->
                                        DropdownMenuItem(
                                            text = { Text("${client.name} ${client.lastName}".trim()) },
                                            onClick = { selectedClient = client; clientDropdownExpanded = false },
                                            leadingIcon = {
                                                Icon(Icons.Default.Person, null,
                                                    tint = MaterialTheme.colorScheme.primary)
                                            })
                                    }
                                }
                            }
                        }
                    }

                    // ── Fecha y Hora ──────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("FECHA Y HORA *", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CalendarToday, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Text(
                                    dateStr.ifBlank { "mm/dd/yyyy" },
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (dateStr.isNotBlank()) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.Default.EditCalendar, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { timePicker.show() },
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Schedule, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                                Text(
                                    timeStr.ifBlank { "--:--" },
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (timeStr.isNotBlank()) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(Icons.Default.AccessTime, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // ── Motivo ────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("MOTIVO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            serviceTypes.forEach { st ->
                                val selected = type == st
                                Surface(
                                    onClick = { type = st },
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                                    shadowElevation = if (selected) 0.dp else 2.dp,
                                    border = if (selected) null else
                                        androidx.compose.foundation.BorderStroke(
                                            1.dp, MaterialTheme.colorScheme.outlineVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(serviceIcons[st]!!, null,
                                            tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp))
                                        Text(serviceLabels[st]!!,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    // ── Tipo de Equipo ────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TIPO DE EQUIPO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EQUIPMENT_TYPES.chunked(2).forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { et ->
                                        val selected = equipmentType == et
                                        Surface(
                                            onClick = { equipmentType = if (equipmentType == et) "" else et },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (selected)
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            else Color.White,
                                            shadowElevation = if (selected) 0.dp else 2.dp,
                                            border = if (selected)
                                                androidx.compose.foundation.BorderStroke(
                                                    1.5.dp, MaterialTheme.colorScheme.primary)
                                            else
                                                androidx.compose.foundation.BorderStroke(
                                                    1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.size(32.dp)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                            MaterialTheme.colorScheme.primary.copy(
                                                                alpha = if (selected) 0.18f else 0.10f
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        equipmentIcons[et] ?: Icons.Default.Build,
                                                        null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(et,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                                    color = if (selected) MaterialTheme.colorScheme.primary
                                                            else MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Notas adicionales ─────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("NOTAS ADICIONALES", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                placeholder = { Text("Ej. El cliente solicita llamar antes de llegar…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
