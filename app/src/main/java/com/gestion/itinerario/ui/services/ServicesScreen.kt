package com.gestion.itinerario.ui.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.*
import com.gestion.itinerario.ui.agenda.EQUIPMENT_TYPES
import com.gestion.itinerario.ui.invoice.InvoiceCreationDialog
import com.gestion.itinerario.ui.theme.*
import com.gestion.itinerario.data.entity.FaultClassifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: ServiceViewModel = hiltViewModel(),
    agendaViewModel: com.gestion.itinerario.ui.agenda.AgendaViewModel = hiltViewModel(),
    profileViewModel: com.gestion.itinerario.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val orders                by viewModel.orders.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    val clients by viewModel.clientRepo.getAll().collectAsStateWithLifecycle(emptyList())
    val companyProfile by profileViewModel.profile.collectAsStateWithLifecycle()
    val clientMap = remember(clients) { clients.associateBy { it.id } }
    fun clientFullName(id: String) = clientMap[id]?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"
    val professionalName = companyProfile.ownerName.ifBlank { companyProfile.companyName }.ifBlank { "Profesional asignado" }
    var detailAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showDialog   by remember { mutableStateOf(false) }
    var editOrder    by remember { mutableStateOf<ServiceOrder?>(null) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var editAppointment by remember { mutableStateOf<Appointment?>(null) }
    var filterStatus by remember { mutableStateOf<ServiceStatus?>(null) }
    var showQuotes by remember { mutableStateOf(false) }

    // Diálogo de "Programar próximo mantenimiento" al completar cita
    var completedAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Diálogo de factura para órdenes completadas
    var invoiceOrder by remember { mutableStateOf<ServiceOrder?>(null) }

    // Diálogo de factura/PDF al completar una cita
    var invoiceAppointment by remember { mutableStateOf<Appointment?>(null) }

    // Diálogo de confirmación genérico
    var confirmTitle   by remember { mutableStateOf("") }
    var confirmMessage by remember { mutableStateOf("") }
    var confirmAction  by remember { mutableStateOf<(() -> Unit)?>(null) }

    val filteredOrders = if (filterStatus != null) orders.filter { it.status == filterStatus } else orders

    val filteredAppointments = if (filterStatus != null) {
        allAppointments.filter { appt ->
            when (filterStatus) {
                ServiceStatus.PENDING -> appt.status == AppointmentStatus.SCHEDULED
                ServiceStatus.IN_PROGRESS -> appt.status == AppointmentStatus.IN_PROGRESS
                ServiceStatus.COMPLETED -> appt.status == AppointmentStatus.COMPLETED
                else -> false
            }
        }
    } else {
        // En "Todos" mostramos solo pendientes y en proceso
        allAppointments.filter { it.status == AppointmentStatus.SCHEDULED || it.status == AppointmentStatus.IN_PROGRESS }
    }

    // Diálogo de confirmación
    if (confirmAction != null) {
        AlertDialog(
            onDismissRequest = { confirmAction = null },
            icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF8F00)) },
            title = { Text(confirmTitle, fontWeight = FontWeight.Bold) },
            text = { Text(confirmMessage) },
            confirmButton = {
                Button(
                    onClick = { confirmAction?.invoke(); confirmAction = null },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusLowStock)
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmAction = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Métricas semanales ────────────────────────────────────────────────────
    val weekAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
    val weeklyCompleted = orders.count { it.status == ServiceStatus.COMPLETED && (it.completedAt ?: 0L) >= weekAgo }
    val weeklyTotal = orders.count { it.createdAt >= weekAgo }
    val efficiency = if (weeklyTotal > 0) weeklyCompleted * 100 / weeklyTotal else 100

    // ── Citas de hoy ─────────────────────────────────────────────────────────
    val todayStart = remember {
        java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayCount = filteredAppointments.count { it.dateTime >= todayStart && it.dateTime < todayStart + 86_400_000L }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servicios", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showQuotes = true }) {
                        Icon(Icons.Default.RequestQuote, contentDescription = "Cotizaciones")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { editOrder = null; showDialog = true }, containerColor = Primary40) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            // ── Tabs de filtro personalizados ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ServiceFilterTab("Todos", selected = filterStatus == null) { filterStatus = null }
                ServiceFilterTab("Pendiente", selected = filterStatus == ServiceStatus.PENDING) { filterStatus = ServiceStatus.PENDING }
                ServiceFilterTab("En Proceso", selected = filterStatus == ServiceStatus.IN_PROGRESS) { filterStatus = ServiceStatus.IN_PROGRESS }
            }

            val noOrders       = filteredOrders.isEmpty()
            val noAppointments = filteredAppointments.isEmpty()

            if (noOrders && noAppointments) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Engineering, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp))
                        Text("Sin órdenes encontradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp,
                        top = 4.dp,
                        bottom = innerPadding.calculateBottomPadding() + 88.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── Citas Programadas ─────────────────────────────────
                    if (filteredAppointments.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Citas Programadas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                if (todayCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Primary40
                                    ) {
                                        Text(
                                            "$todayCount Hoy",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        items(filteredAppointments, key = { "appt_${it.id}" }) { appt ->
                            val cn = clientFullName(appt.clientId).let {
                                if (it == "Cliente" && appt.notes.isNotBlank())
                                    appt.notes.substringBefore(" —").ifBlank { it } else it
                            }
                            ScheduledAppointmentCard(
                                appointment   = appt,
                                clientName    = cn,
                                onViewDetails = { detailAppointment = appt },
                                onEdit        = { editAppointment = appt; showAppointmentDialog = true },
                                onStart       = { viewModel.startAppointment(appt) },
                                onComplete    = {
                                    viewModel.completeAppointment(appt)
                                    invoiceAppointment = appt
                                },
                                onCancel      = {
                                    confirmTitle   = "Cancelar cita"
                                    confirmMessage = "¿Estás segura de que deseas cancelar esta cita?"
                                    confirmAction  = { viewModel.cancelAppointment(appt) }
                                }
                            )
                        }
                    }

                    // ── Órdenes de Servicio ───────────────────────────────
                    if (filteredOrders.isNotEmpty()) {
                        item {
                            if (filteredAppointments.isNotEmpty()) Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Órdenes de Servicio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Secondary40.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        "${filteredOrders.size}",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Secondary40
                                    )
                                }
                            }
                        }
                        items(filteredOrders, key = { "order_${it.id}" }) { order ->
                            ServiceOrderCard(order,
                                onEdit   = { editOrder = order; showDialog = true },
                                onDelete = {
                                    confirmTitle   = "Eliminar orden"
                                    confirmMessage = "¿Estás segura de que deseas eliminar esta orden? Esta acción no se puede deshacer."
                                    confirmAction  = { viewModel.delete(order) }
                                },
                                onStatusChange = { newStatus ->
                                    viewModel.updateStatus(order, newStatus)
                                    if (newStatus == ServiceStatus.COMPLETED) invoiceOrder = order
                                },
                                onInvoice = { invoiceOrder = order }
                            )
                        }
                    }

                    // ── Métricas Semanales ────────────────────────────────
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        Text(
                            "Métricas Semanales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ServiceMetricTile(
                                modifier  = Modifier.weight(1f),
                                value     = "$weeklyCompleted",
                                label     = "COMPLETADOS",
                                icon      = Icons.Default.CheckCircle,
                                gradient  = Brush.linearGradient(listOf(Primary40, Color(0xFF5B21B6)))
                            )
                            ServiceMetricTile(
                                modifier  = Modifier.weight(1f),
                                value     = "$efficiency%",
                                label     = "EFICIENCIA",
                                icon      = Icons.Default.TrendingUp,
                                gradient  = Brush.linearGradient(listOf(Secondary40, Color(0xFF9D174D)))
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        ServiceOrderFormDialog(
            initial   = editOrder,
            existingOrders = orders,
            clients   = clients,
            onDismiss = { showDialog = false },
            onSave    = { o ->
                if (editOrder == null) viewModel.save(o) else viewModel.update(o.copy(id = editOrder!!.id))
                showDialog = false
            }
        )
    }

    if (showAppointmentDialog) {
        val clients by viewModel.clientRepo.getAll().collectAsStateWithLifecycle(emptyList())
        com.gestion.itinerario.ui.agenda.AppointmentFormDialog(
            initial   = editAppointment,
            clients   = clients,
            viewModel = agendaViewModel,
            onDismiss = { showAppointmentDialog = false },
            onSave    = { a ->
                if (editAppointment == null) agendaViewModel.save(a)
                else agendaViewModel.update(a.copy(id = editAppointment!!.id))
                showAppointmentDialog = false
            }
        )
    }

    // ── Diálogo de factura para orden de servicio ─────────────────────────────
    invoiceOrder?.let { order ->
        val clients by viewModel.clientRepo.getAll().collectAsStateWithLifecycle(emptyList())
        val client = clients.firstOrNull { it.id == order.clientId }
        InvoiceCreationDialog(
            serviceOrderId     = order.id,
            clientId           = order.clientId,
            clientName         = client?.let { "${it.name} ${it.lastName}".trim() } ?: "",
            clientPhone        = client?.phone ?: "",
            clientAddress      = client?.address ?: "",
            equipmentType      = order.equipmentType,
            serviceDescription = order.description,
            diagnosis          = order.diagnosis,
            totalAmount        = order.totalCost,
            paymentMethod      = order.paymentMethod,
            paymentStatus      = order.paymentStatus,
            startDate          = order.createdAt,
            onDismiss          = { invoiceOrder = null },
            onInvoiceCreated   = { invoiceId, _ ->
                viewModel.update(order.copy(invoiceId = invoiceId))
                invoiceOrder = null
            }
        )
    }

    // ── Diálogo de factura/PDF al completar una cita ─────────────────────────
    invoiceAppointment?.let { appt ->
        val clients by viewModel.clientRepo.getAll().collectAsStateWithLifecycle(emptyList())
        val client = clients.firstOrNull { it.id == appt.clientId }
        val clientName = client?.let { "${it.name} ${it.lastName}".trim() }
            ?: appt.notes.substringBefore(" —").ifBlank { "" }
        val serviceDesc = when (appt.serviceType) {
            ServiceType.MAINTENANCE  -> "Mantenimiento"
            ServiceType.REPAIR       -> "Reparación"
            ServiceType.INSTALLATION -> "Instalación"
        } + if (appt.equipmentType.isNotBlank()) " - ${appt.equipmentType}" else ""
        InvoiceCreationDialog(
            appointmentId      = appt.id,
            clientId           = appt.clientId,
            clientName         = clientName,
            clientPhone        = client?.phone ?: "",
            clientAddress      = client?.address ?: "",
            equipmentType      = appt.equipmentType,
            serviceDescription = serviceDesc,
            diagnosis          = "",
            totalAmount        = 0.0,
            paymentMethod      = PaymentMethod.NONE,
            paymentStatus      = PaymentStatus.NONE,
            startDate          = appt.dateTime,
            onDismiss = {
                invoiceAppointment = null
                completedAppointment = appt
            },
            onInvoiceCreated = { _, _ ->
                invoiceAppointment = null
                completedAppointment = appt
            }
        )
    }

    // ── Diálogo "Programar próximo mantenimiento" ─────────────────────────────
    completedAppointment?.let { appt ->
        NextMaintenanceDialog(
            appointment = appt,
            onDismiss   = { completedAppointment = null },
            onSchedule  = { intervalValue, intervalUnit ->
                viewModel.scheduleNextMaintenance(
                    equipmentId   = appt.equipmentId ?: "",
                    clientId      = appt.clientId,
                    intervalValue = intervalValue,
                    intervalUnit  = intervalUnit,
                    notes         = "Auto: ${appt.equipmentType.ifBlank { appt.serviceType.name }}"
                )
                completedAppointment = null
            }
        )
    }

    // ── Diálogo de cotizaciones previas ───────────────────────────────────────
    if (showQuotes) {
        com.gestion.itinerario.ui.quotes.QuotesDialog(onDismiss = { showQuotes = false })
    }

    // ── Pantalla de Detalle del Servicio (desde Citas Programadas) ────────────
    detailAppointment?.let { appt ->
        ServiceDetailScreen(
            appointment = appt,
            clientName = clientFullName(appt.clientId),
            professionalName = professionalName,
            clientPhone = clientMap[appt.clientId]?.phone ?: "",
            onDismiss = { detailAppointment = null }
        )
    }
}

// ─── Diálogo de próximo mantenimiento ────────────────────────────────────────
private data class MaintInterval(val value: Int, val unit: IntervalUnit, val label: String)

private val MAINT_OPTIONS = listOf(
    MaintInterval(1, IntervalUnit.WEEKS,  "1 semana"),
    MaintInterval(2, IntervalUnit.WEEKS,  "2 semanas"),
    MaintInterval(1, IntervalUnit.MONTHS, "1 mes"),
    MaintInterval(2, IntervalUnit.MONTHS, "2 meses"),
    MaintInterval(3, IntervalUnit.MONTHS, "3 meses"),
    MaintInterval(6, IntervalUnit.MONTHS, "6 meses")
)

@Composable
private fun NextMaintenanceDialog(
    appointment: Appointment,
    onDismiss: () -> Unit,
    onSchedule: (Int, IntervalUnit) -> Unit
) {
    var selected by remember { mutableStateOf(MAINT_OPTIONS[4]) } // default 3 meses
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.NotificationsActive, null, tint = Primary80) },
        title = { Text("¿Programar próximo mantenimiento?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Servicio completado: ${appointment.equipmentType.ifBlank { appointment.serviceType.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Intervalo:", style = MaterialTheme.typography.labelMedium)
                // Semanas
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MAINT_OPTIONS.filter { it.unit == IntervalUnit.WEEKS }.forEach { opt ->
                        FilterChip(selected = selected == opt, onClick = { selected = opt },
                            label = { Text(opt.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                // Meses
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MAINT_OPTIONS.filter { it.unit == IntervalUnit.MONTHS }.forEach { opt ->
                        FilterChip(selected = selected == opt, onClick = { selected = opt },
                            label = { Text(opt.label, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                // Fecha estimada
                val cal = java.util.Calendar.getInstance().apply {
                    when (selected.unit) {
                        IntervalUnit.WEEKS  -> add(java.util.Calendar.WEEK_OF_YEAR, selected.value)
                        IntervalUnit.MONTHS -> add(java.util.Calendar.MONTH, selected.value)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(0.5f)) {
                    Row(modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
            Button(onClick = { onSchedule(selected.value, selected.unit) }) {
                Text("Programar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Omitir") }
        }
    )
}

// ─── Encabezado de sección ────────────────────────────────────────────────────
@Composable
fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    count: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, null, tint = Primary40, modifier = Modifier.size(18.dp))
        Text(title, fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground)
        Surface(shape = RoundedCornerShape(10.dp), color = Primary40.copy(alpha = 0.12f)) {
            Text("$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall, color = Primary40,
                fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Tab de filtro personalizado ─────────────────────────────────────────────
@Composable
private fun ServiceFilterTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (selected) Modifier.background(
                        Brush.linearGradient(listOf(Primary40, Secondary40)),
                        RoundedCornerShape(50)
                    ) else Modifier
                )
                .padding(horizontal = 18.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── Tile de métrica semanal ──────────────────────────────────────────────────
@Composable
private fun ServiceMetricTile(
    modifier: Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush
) {
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(22.dp))
            Column {
                Text(value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White)
                Text(label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp)
            }
        }
    }
}

// ─── Tarjeta de cita programada ───────────────────────────────────────────────
@Composable
fun ScheduledAppointmentCard(
    appointment: Appointment,
    clientName: String,
    onViewDetails: () -> Unit = {},
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val sdfDate = remember { SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) }
    val sdfTime = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val tipoLabel = appointment.serviceType.displayName()

    val isDark = appointment.status == AppointmentStatus.IN_PROGRESS
    val cardBg = if (isDark) DarkBackground else Color.White
    val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val subtextColor = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant

    val (statusColor, statusLabel) = when (appointment.status) {
        AppointmentStatus.SCHEDULED   -> StatusPending   to "PROGRAMADA"
        AppointmentStatus.IN_PROGRESS -> StatusInRepair  to "EN PROCESO"
        AppointmentStatus.COMPLETED   -> StatusCompleted to "COMPLETADA"
        AppointmentStatus.CANCELLED   -> StatusLowStock  to "CANCELADA"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Borde izquierdo degradado
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(listOf(Primary40, Secondary40)),
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Nombre + estado
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = if (isDark) 0.22f else 0.12f)
                    ) {
                        Text(
                            statusLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
                // Tipo de servicio
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Build, null,
                        tint = subtextColor, modifier = Modifier.size(13.dp))
                    Text(tipoLabel, style = MaterialTheme.typography.bodySmall, color = subtextColor)
                }
                // Equipo (si hay)
                if (appointment.equipmentType.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Settings, null,
                            tint = subtextColor, modifier = Modifier.size(13.dp))
                        Text(appointment.equipmentType,
                            style = MaterialTheme.typography.bodySmall, color = subtextColor)
                    }
                }
                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null,
                            tint = subtextColor, modifier = Modifier.size(13.dp))
                        Text(sdfDate.format(Date(appointment.dateTime)),
                            style = MaterialTheme.typography.bodySmall, color = subtextColor)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, null,
                            tint = subtextColor, modifier = Modifier.size(13.dp))
                        Text(sdfTime.format(Date(appointment.dateTime)),
                            style = MaterialTheme.typography.bodySmall, color = subtextColor)
                    }
                }
                // Botones de acción
                if (appointment.status != AppointmentStatus.COMPLETED &&
                    appointment.status != AppointmentStatus.CANCELLED) {
                    Spacer(Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (appointment.status) {
                            AppointmentStatus.SCHEDULED -> {
                                Button(
                                    onClick = onStart,
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Primary40),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("EN PROCESO",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            AppointmentStatus.IN_PROGRESS -> {
                                Button(
                                    onClick = onComplete,
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("COMPLETAR",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            else -> {}
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp, if (isDark) Color.White.copy(0.3f) else MaterialTheme.colorScheme.outline.copy(0.4f)
                            ),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("CANCELAR",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // Ver detalles link (small)
                if (appointment.status != AppointmentStatus.CANCELLED) {
                    TextButton(
                        onClick = onViewDetails,
                        modifier = Modifier.align(Alignment.End).height(28.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text("Ver detalles",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) Color.White.copy(0.7f) else MaterialTheme.colorScheme.primary)
                        Icon(Icons.Default.ArrowForward, null,
                            modifier = Modifier.size(12.dp),
                            tint = if (isDark) Color.White.copy(0.7f) else MaterialTheme.colorScheme.primary)
                    }
                }
            }
            // Botón editar (top right)
            if (appointment.status != AppointmentStatus.COMPLETED && appointment.status != AppointmentStatus.CANCELLED) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.align(Alignment.Top).padding(top = 6.dp, end = 4.dp).size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, null,
                        tint = if (isDark) Color.White.copy(0.7f) else Primary40,
                        modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── Extension functions ──────────────────────────────────────────────────────

fun ServiceStatus.displayName() = when (this) {
    ServiceStatus.PENDING     -> "Pendiente"
    ServiceStatus.IN_PROGRESS -> "En Proceso"
    ServiceStatus.COMPLETED   -> "Finalizado"
}

fun ServiceStatus.color() = when (this) {
    ServiceStatus.PENDING     -> StatusPending
    ServiceStatus.IN_PROGRESS -> StatusInRepair
    ServiceStatus.COMPLETED   -> StatusCompleted
}

fun ServiceType.displayName() = when (this) {
    ServiceType.MAINTENANCE  -> "Mantenimiento"
    ServiceType.REPAIR       -> "Reparación"
    ServiceType.INSTALLATION -> "Instalación"
}

// ─── Tarjeta de orden de servicio ─────────────────────────────────────────────
@Composable
fun ServiceOrderCard(
    order: ServiceOrder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStatusChange: (ServiceStatus) -> Unit,
    onInvoice: (() -> Unit)? = null
) {
    val statusColor = order.status.color()

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, null, tint = Primary80, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.description, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("Tipo: ${order.type.displayName()}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Mostrar tipo de equipo si fue especificado
                    if (order.equipmentType.isNotBlank()) {
                        Text("🔧 ${order.equipmentType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(order.status.displayName(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor, fontWeight = FontWeight.Medium)
                }
            }
            if (order.diagnosis.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text("Diagnóstico: ${order.diagnosis}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            // ── Pago ─────────────────────────────────────────────────────────
            if (order.paymentStatus != PaymentStatus.NONE || order.totalCost > 0.0) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    if (order.totalCost > 0.0) {
                        Text("\$${String.format("%.2f", order.totalCost)}",
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                    if (order.paymentStatus != PaymentStatus.NONE) {
                        val (pColor, pLabel) = when (order.paymentStatus) {
                            PaymentStatus.PAID    -> Color(0xFF2E7D32) to "Pagado"
                            PaymentStatus.PENDING -> Color(0xFFE65100) to "Pendiente"
                            PaymentStatus.NONE    -> MaterialTheme.colorScheme.onSurfaceVariant to ""
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = pColor.copy(alpha = 0.12f)) {
                            Text(pLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall, color = pColor,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                    if (order.paymentMethod != PaymentMethod.NONE) {
                        val methodLabel = when (order.paymentMethod) {
                            PaymentMethod.CASH     -> "Efectivo"
                            PaymentMethod.TRANSFER -> "Transferencia"
                            PaymentMethod.NONE     -> ""
                        }
                        Text(methodLabel, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                if (order.status != ServiceStatus.COMPLETED) {
                    val nextStatus = if (order.status == ServiceStatus.PENDING)
                        ServiceStatus.IN_PROGRESS else ServiceStatus.COMPLETED
                    OutlinedButton(
                        onClick = { onStatusChange(nextStatus) },
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Text("→ ${nextStatus.displayName()}", style = MaterialTheme.typography.labelSmall)
                    }
                } else if (onInvoice != null) {
                    val hasInvoice = order.invoiceId.isNotBlank()
                    OutlinedButton(
                        onClick = onInvoice,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary80)
                    ) {
                        Icon(Icons.Default.Receipt, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (hasInvoice) "Ver Factura" else "Facturar",
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, null, tint = StatusLowStock, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ─── Formulario de orden de servicio (Dialog full-scroll) ────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceOrderFormDialog(
    initial: ServiceOrder?,
    existingOrders: List<ServiceOrder> = emptyList(),
    clients: List<com.gestion.itinerario.data.entity.Client> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (ServiceOrder) -> Unit
) {
    var desc          by remember { mutableStateOf(initial?.description ?: "") }
    var diagnosis     by remember { mutableStateOf(initial?.diagnosis ?: "") }
    var type          by remember { mutableStateOf(initial?.type ?: ServiceType.MAINTENANCE) }
    var equipmentType by remember { mutableStateOf(initial?.equipmentType ?: "") }
    var clientId      by remember { mutableStateOf(initial?.clientId ?: "") }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var equipId       by remember { mutableStateOf(initial?.equipmentId ?: "") }
    var totalCostStr  by remember { mutableStateOf(if ((initial?.totalCost ?: 0.0) > 0.0) initial!!.totalCost.toString() else "") }
    var paymentMethod by remember { mutableStateOf(initial?.paymentMethod ?: PaymentMethod.NONE) }
    var paymentStatus by remember { mutableStateOf(initial?.paymentStatus ?: PaymentStatus.NONE) }
    var warrantyMonths by remember { mutableStateOf(initial?.warrantyMonths ?: 0) }

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

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text(if (initial == null) "Nueva Orden de Servicio" else "Editar Orden",
                            fontWeight = FontWeight.Bold) },
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
                                    onSave(ServiceOrder(
                                        id = initial?.id ?: "",
                                        clientId = clientId,
                                        equipmentId = equipId,
                                        type = type,
                                        description = desc,
                                        diagnosis = diagnosis,
                                        equipmentType = equipmentType,
                                        totalCost = totalCostStr.toDoubleOrNull() ?: 0.0,
                                        paymentMethod = paymentMethod,
                                        paymentStatus = paymentStatus,
                                        status = initial?.status ?: ServiceStatus.PENDING,
                                        createdAt = initial?.createdAt ?: System.currentTimeMillis(),
                                        completedAt = initial?.completedAt,
                                        warrantyMonths = warrantyMonths,
                                        warrantyExpiresAt = initial?.warrantyExpiresAt
                                    ))
                                },
                                enabled = desc.isNotBlank(),
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
                                        Text("Guardar", color = Color.White, fontWeight = FontWeight.SemiBold)
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
                                    Icon(Icons.Default.Engineering, null,
                                        tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(if (initial == null) "Registrar Servicio" else "Editar Servicio",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    Text("Ingrese los datos del trabajo a realizar.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // ── Motivo ────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TIPO DE SERVICIO", style = MaterialTheme.typography.labelSmall,
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

                    // ── Descripción del problema ──────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("DESCRIPCIÓN DEL PROBLEMA *", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = desc,
                                onValueChange = {
                                    desc = it
                                    FaultClassifier.classify(it)?.let { sug -> type = sug.suggestedType }
                                },
                                placeholder = { Text("Describa el problema o falla del equipo…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(), minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        // Sugerencia automática del FaultClassifier
                        val suggestion = remember(desc) { FaultClassifier.classify(desc) }
                        if (suggestion != null) {
                            Surface(shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(0.6f)) {
                                Row(modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lightbulb, null,
                                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Causa probable:", style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold)
                                        Text(suggestion.possibleCause, style = MaterialTheme.typography.bodySmall)
                                        TextButton(onClick = { diagnosis = suggestion.possibleCause },
                                            contentPadding = PaddingValues(0.dp),
                                            modifier = Modifier.height(24.dp)) {
                                            Text("Usar como diagnóstico →", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Diagnóstico ───────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("DIAGNÓSTICO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = diagnosis, onValueChange = { diagnosis = it },
                                placeholder = { Text("Diagnóstico técnico…",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                modifier = Modifier.fillMaxWidth(), minLines = 2,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // ── IDs ───────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DATOS ADICIONALES", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        // ── Dropdown de cliente ───────────────────────────
                        if (clients.isNotEmpty()) {
                            val selectedClientName = clients.firstOrNull { it.id == clientId }
                                ?.let { "${it.name} ${it.lastName}".trim() } ?: ""
                            Box {
                                Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                    shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = selectedClientName.ifBlank { "Seleccione un cliente" },
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("CLIENTE") },
                                        leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                                        trailingIcon = {
                                            Icon(
                                                if (clientDropdownExpanded) Icons.Default.KeyboardArrowUp
                                                else Icons.Default.KeyboardArrowDown,
                                                null
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                                Box(modifier = Modifier.matchParentSize().clickable { clientDropdownExpanded = true })
                                DropdownMenu(
                                    expanded = clientDropdownExpanded,
                                    onDismissRequest = { clientDropdownExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Sin cliente asignado", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                        onClick = { clientId = ""; clientDropdownExpanded = false }
                                    )
                                    clients.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.name} ${c.lastName}".trim()) },
                                            leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                                            onClick = { clientId = c.id; clientDropdownExpanded = false }
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = clientId, onValueChange = { clientId = it },
                                    label = { Text("ID Cliente") },
                                    leadingIcon = { Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)) },
                                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = equipId, onValueChange = { equipId = it },
                                label = { Text("ID Equipo") },
                                leadingIcon = { Icon(Icons.Default.Build, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        // Alerta de garantía vigente
                        val now = System.currentTimeMillis()
                        val activeWarrantyOrder = remember(equipId, existingOrders) {
                            existingOrders.filter {
                                it.equipmentId.isNotBlank() && it.equipmentId == equipId &&
                                    it.id != (initial?.id ?: "") &&
                                    it.status == ServiceStatus.COMPLETED &&
                                    it.warrantyExpiresAt != null && it.warrantyExpiresAt > now
                            }.maxByOrNull { it.completedAt ?: 0L }
                        }
                        if (activeWarrantyOrder != null) {
                            val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
                            Surface(shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(0.6f)) {
                                Row(modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.VerifiedUser, null,
                                        tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                    Column {
                                        Text("Equipo en garantía", style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "Garantía vigente hasta ${sdf.format(Date(activeWarrantyOrder.warrantyExpiresAt!!))} " +
                                                "(\"${activeWarrantyOrder.description.take(60)}\"). Si la falla es similar, podría estar cubierta.",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── Pago ──────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("PAGO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = totalCostStr,
                                onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) totalCostStr = v },
                                label = { Text("Monto (USD)") },
                                leadingIcon = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(18.dp)) },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                placeholder = { Text("0.00") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                        Text("Método de pago", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PaymentMethod.NONE to "Sin especificar",
                                PaymentMethod.CASH to "Efectivo",
                                PaymentMethod.TRANSFER to "Transferencia").forEach { (m, label) ->
                                FilterChip(selected = paymentMethod == m, onClick = { paymentMethod = m },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                            }
                        }
                        Text("Estado del pago", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(PaymentStatus.NONE to "Sin especificar",
                                PaymentStatus.PENDING to "Pendiente",
                                PaymentStatus.PAID to "Pagado").forEach { (s, label) ->
                                FilterChip(selected = paymentStatus == s, onClick = { paymentStatus = s },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                            }
                        }
                    }

                    // ── Garantía ──────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("GARANTÍA DEL SERVICIO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                            listOf(0 to "Sin garantía", 1 to "1 mes", 3 to "3 meses",
                                6 to "6 meses", 12 to "12 meses").forEach { (m, label) ->
                                FilterChip(selected = warrantyMonths == m, onClick = { warrantyMonths = m },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
