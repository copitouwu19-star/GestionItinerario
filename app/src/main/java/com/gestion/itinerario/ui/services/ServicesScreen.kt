package com.gestion.itinerario.ui.services

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
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
    agendaViewModel: com.gestion.itinerario.ui.agenda.AgendaViewModel = hiltViewModel()
) {
    val orders                by viewModel.orders.collectAsStateWithLifecycle()
    val allAppointments by viewModel.allAppointments.collectAsStateWithLifecycle()
    var showDialog   by remember { mutableStateOf(false) }
    var editOrder    by remember { mutableStateOf<ServiceOrder?>(null) }
    var showAppointmentDialog by remember { mutableStateOf(false) }
    var editAppointment by remember { mutableStateOf<Appointment?>(null) }
    var filterStatus by remember { mutableStateOf<ServiceStatus?>(null) }

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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Órdenes de Servicio", fontWeight = FontWeight.Bold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editOrder = null; showDialog = true }, containerColor = Primary40) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(
            top    = padding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding()
        )) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filterStatus == null, onClick = { filterStatus = null }, label = { Text("Todos") })
                ServiceStatus.values().forEach { s ->
                    FilterChip(
                        selected = filterStatus == s,
                        onClick  = { filterStatus = if (filterStatus == s) null else s },
                        label    = { Text(s.displayName()) }
                    )
                }
            }

            val noOrders       = filteredOrders.isEmpty()
            val noAppointments = filteredAppointments.isEmpty()

            if (noOrders && noAppointments) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Engineering, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp))
                        Text("Sin órdenes encontradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // ── Sección: Órdenes de Servicio ─────────────────────────
                    if (filteredOrders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                icon  = Icons.Default.Build,
                                title = "Órdenes de Servicio",
                                count = filteredOrders.size
                            )
                        }
                        items(filteredOrders, key = { "order_${it.id}" }) { order ->
                            ServiceOrderCard(order,
                                onEdit   = { editOrder = order; showDialog = true },
                                onDelete = {
                                    confirmTitle   = "Eliminar orden"
                                    confirmMessage = "¿Estás segura de que deseas eliminar esta orden de servicio? Esta acción no se puede deshacer."
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

                    // ── Sección: Citas Programadas ───────────────────────────
                    if (filteredAppointments.isNotEmpty()) {
                        item {
                            if (filteredOrders.isNotEmpty()) Spacer(Modifier.height(4.dp))
                            SectionHeader(
                                icon  = Icons.Default.EventAvailable,
                                title = "Citas Programadas",
                                count = filteredAppointments.size
                            )
                        }
                        items(filteredAppointments, key = { "appt_${it.id}" }) { appt ->
                            ScheduledAppointmentCard(
                                appointment = appt,
                                onEdit      = { editAppointment = appt; showAppointmentDialog = true },
                                onStart     = { viewModel.startAppointment(appt) },
                                onComplete  = {
                                    viewModel.completeAppointment(appt)
                                    invoiceAppointment = appt
                                },
                                onCancel    = {
                                    confirmTitle   = "Cancelar cita"
                                    confirmMessage = "¿Estás segura de que deseas cancelar esta cita? Esta acción no se puede deshacer."
                                    confirmAction  = { viewModel.cancelAppointment(appt) }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        ServiceOrderFormDialog(
            initial   = editOrder,
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
        Icon(icon, null, tint = Primary80, modifier = Modifier.size(18.dp))
        Text(title, fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onBackground)
        Surface(shape = RoundedCornerShape(10.dp), color = Primary80.copy(alpha = 0.12f)) {
            Text("$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall, color = Primary80)
        }
    }
}

// ─── Tarjeta de cita programada (con ciclo SCHEDULED → IN_PROGRESS → COMPLETED) ──
@Composable
fun ScheduledAppointmentCard(
    appointment: Appointment,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val isMantenimiento = appointment.serviceType == ServiceType.MAINTENANCE
    val accentColor = if (isMantenimiento) Color(0xFF2E7D32) else Color(0xFF1565C0)
    val tipoLabel = when (appointment.serviceType) {
        ServiceType.MAINTENANCE  -> "Mantenimiento"
        ServiceType.REPAIR       -> "Reparación"
        ServiceType.INSTALLATION -> "Instalación"
    }
    val clientName = appointment.notes.substringBefore(" —").ifBlank { appointment.notes }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isMantenimiento) Icons.Default.Build else Icons.Default.EventAvailable,
                    null, tint = accentColor, modifier = Modifier.size(18.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(clientName, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(sdf.format(Date(appointment.dateTime)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    // Tipo de equipo (si fue especificado)
                    if (appointment.equipmentType.isNotBlank()) {
                        Text("🔧 ${appointment.equipmentType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = accentColor.copy(alpha = 0.15f)) {
                    Text(tipoLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor)
                }
            }

            // Badge de estado
            val (statusLabel, statusColor) = when (appointment.status) {
                AppointmentStatus.SCHEDULED   -> "Programada" to StatusPending
                AppointmentStatus.IN_PROGRESS -> "En Proceso" to StatusInRepair
                else                          -> "Programada" to StatusPending
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.12f),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text(statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor)
            }

            Spacer(Modifier.height(8.dp))

            // Botones según el estado actual del ciclo
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (appointment.status) {
                    AppointmentStatus.SCHEDULED -> {
                        // Pendiente → En Proceso
                        OutlinedButton(
                            onClick = onStart,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp)
                        ) {
                            Text("→ En Proceso", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    AppointmentStatus.IN_PROGRESS -> {
                        // En Proceso → Completar
                        OutlinedButton(
                            onClick = onComplete,
                            modifier = Modifier.height(30.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCompleted)
                        ) {
                            Text("✓ Completar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    else -> {}
                }
                if (appointment.status != AppointmentStatus.COMPLETED && appointment.status != AppointmentStatus.CANCELLED) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLowStock)
                    ) {
                        Text("✕ Cancelar", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.weight(1f))
                if (appointment.status != AppointmentStatus.COMPLETED && appointment.status != AppointmentStatus.CANCELLED) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(16.dp))
                    }
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

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
@Composable
fun ServiceOrderFormDialog(initial: ServiceOrder?, onDismiss: () -> Unit, onSave: (ServiceOrder) -> Unit) {
    var desc          by remember { mutableStateOf(initial?.description ?: "") }
    var diagnosis     by remember { mutableStateOf(initial?.diagnosis ?: "") }
    var type          by remember { mutableStateOf(initial?.type ?: ServiceType.MAINTENANCE) }
    var equipmentType by remember { mutableStateOf(initial?.equipmentType ?: "") }
    var clientId      by remember { mutableStateOf(initial?.clientId ?: "") }
    var equipId       by remember { mutableStateOf(initial?.equipmentId ?: "") }
    var totalCostStr  by remember { mutableStateOf(if ((initial?.totalCost ?: 0.0) > 0.0) initial!!.totalCost.toString() else "") }
    var paymentMethod by remember { mutableStateOf(initial?.paymentMethod ?: PaymentMethod.NONE) }
    var paymentStatus by remember { mutableStateOf(initial?.paymentStatus ?: PaymentStatus.NONE) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Título ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (initial == null) "Nueva Orden de Servicio" else "Editar Orden",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()

                // ── Contenido scrolleable ─────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tipo de servicio
                    Text("Tipo de servicio", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ServiceType.values().forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t },
                                label = { Text(t.displayName(), style = MaterialTheme.typography.labelSmall) })
                        }
                    }

                    // Tipo de equipo
                    Text("Tipo de equipo", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        EQUIPMENT_TYPES.take(2).forEach { et ->
                            FilterChip(selected = equipmentType == et,
                                onClick = { equipmentType = if (equipmentType == et) "" else et },
                                modifier = Modifier.weight(1f),
                                label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        EQUIPMENT_TYPES.drop(2).forEach { et ->
                            FilterChip(selected = equipmentType == et,
                                onClick = { equipmentType = if (equipmentType == et) "" else et },
                                modifier = Modifier.weight(1f),
                                label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                        }
                    }

                    OutlinedTextField(value = desc, onValueChange = {
                            desc = it
                            FaultClassifier.classify(it)?.let { sug -> type = sug.suggestedType }
                        },
                        label = { Text("Descripción del problema *") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.fillMaxWidth(), minLines = 2)

                    // Sugerencia automática
                    val suggestion = remember(desc) { FaultClassifier.classify(desc) }
                    if (suggestion != null) {
                        Surface(shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(0.6f)) {
                            Row(modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lightbulb, null,
                                    tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
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
                    OutlinedTextField(value = diagnosis, onValueChange = { diagnosis = it },
                        label = { Text("Diagnóstico") },
                        leadingIcon = { Icon(Icons.Default.Engineering, null) },
                        modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = clientId, onValueChange = { clientId = it },
                        label = { Text("ID Cliente") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = equipId, onValueChange = { equipId = it },
                        label = { Text("ID Equipo") },
                        leadingIcon = { Icon(Icons.Default.Build, null) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true)

                    // ── Sección Pago ──────────────────────────────────────────
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AttachMoney, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("Pago", style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedTextField(
                        value = totalCostStr,
                        onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) totalCostStr = v },
                        label = { Text("Monto (USD)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("0.00") }
                    )

                    Text("Método de pago", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(PaymentMethod.NONE to "Sin especificar",
                               PaymentMethod.CASH to "Efectivo",
                               PaymentMethod.TRANSFER to "Transferencia").forEach { (m, label) ->
                            FilterChip(selected = paymentMethod == m, onClick = { paymentMethod = m },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                        }
                    }

                    Text("Estado del pago", style = MaterialTheme.typography.labelMedium,
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

                // ── Botones ───────────────────────────────────────────────────
                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
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
                                completedAt = initial?.completedAt
                            ))
                        },
                        enabled = desc.isNotBlank()
                    ) { Text("Guardar") }
                }
            }
        }
    }
}
