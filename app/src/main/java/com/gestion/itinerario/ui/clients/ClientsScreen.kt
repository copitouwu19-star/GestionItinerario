package com.gestion.itinerario.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.Invoice
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
import com.gestion.itinerario.ui.theme.Secondary80
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusInRepair
import com.gestion.itinerario.ui.theme.StatusLowStock
import com.gestion.itinerario.ui.theme.StatusPending
import com.gestion.itinerario.R
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)

private fun openWhatsApp(context: android.content.Context, phone: String, message: String = "") {
    val cleaned = phone.trimStart('+').let {
        if (it.startsWith("0")) "58${it.drop(1)}" else it
    }
    val url = "https://wa.me/$cleaned${if (message.isNotBlank()) "?text=${Uri.encode(message)}" else ""}"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

private val CLIENT_TYPES = listOf("Persona Natural", "Persona Jurídica")
private val PHONE_CODES  = listOf("0414", "0424", "0412", "0422", "0416", "0426")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: ClientViewModel = hiltViewModel()
) {
    val clients    by viewModel.clients.collectAsStateWithLifecycle()
    val search     by viewModel.search.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editClient by remember { mutableStateOf<Client?>(null) }
    var detailClient by remember { mutableStateOf<Client?>(null) }
    var confirmDeleteClient by remember { mutableStateOf<Client?>(null) }

    // Diálogo de confirmación eliminar cliente
    if (confirmDeleteClient != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteClient = null },
            icon  = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFF8F00)) },
            title = { Text("Eliminar cliente", fontWeight = FontWeight.Bold) },
            text  = { Text("¿Estás segura de que deseas eliminar a ${confirmDeleteClient!!.name}${if (confirmDeleteClient!!.lastName.isNotBlank()) " ${confirmDeleteClient!!.lastName}" else ""}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.delete(confirmDeleteClient!!); confirmDeleteClient = null },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusLowStock)
                ) { Text("Sí, eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteClient = null }) { Text("Cancelar") }
            }
        )
    }

    if (detailClient != null) {
        ClientDetailScreen(client = detailClient!!, viewModel = viewModel, onBack = { detailClient = null })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Clientes", fontWeight = FontWeight.Bold) })
            }
        ) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .padding(
                        top    = scaffoldPadding.calculateTopPadding(),
                        bottom = maxOf(
                            innerPadding.calculateBottomPadding(),
                            scaffoldPadding.calculateBottomPadding()
                        ) + 72.dp // Espacio para el FAB
                    )
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = search, onValueChange = viewModel::onSearch,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Buscar por nombre o teléfono…") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(12.dp)
                )
                if (clients.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.People, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp))
                            Text("Sin clientes registrados",
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text("Toca + para agregar el primero",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(clients, key = { it.id }) { c ->
                            ClientCard(c,
                                onView   = { detailClient = c },
                                onEdit   = { editClient = c; showDialog = true },
                                onDelete = { confirmDeleteClient = c })
                        }
                    }
                }
            }
        }

        // ── FAB fuera del Scaffold para que siempre sea visible ───────────────
        FloatingActionButton(
            onClick = { editClient = null; showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end    = 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
            containerColor = Primary40
        ) {
            Icon(Icons.Default.PersonAdd, null, tint = Color.White)
        }
    }

    if (showDialog) {
        ClientFormDialog(
            initial   = editClient,
            onDismiss = { showDialog = false },
            onSave    = { c ->
                if (editClient == null) viewModel.save(c)
                else viewModel.update(c.copy(id = editClient!!.id))
                showDialog = false
            }
        )
    }
}

@Composable
fun ClientCard(c: Client, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick  = onView
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary80.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(c.name.take(1).uppercase(), color = Primary80, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(c.name + if (c.lastName.isNotBlank()) " ${c.lastName}" else "",
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Surface(
                    shape  = RoundedCornerShape(6.dp),
                    color  = if (c.clientType == "Persona Jurídica")
                        Secondary80.copy(alpha = 0.15f)
                    else Primary80.copy(alpha = 0.12f)
                ) {
                    Text(
                        c.clientType,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = if (c.clientType == "Persona Jurídica") Secondary80 else Primary80
                    )
                }
                if (c.phone.isNotBlank()) Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Phone, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp))
                    Text(c.phone, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (c.address.isNotBlank()) Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.LocationOn, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp))
                    Text(c.address, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            if (c.phone.isNotBlank()) {
                IconButton(onClick = { openWhatsApp(context, c.phone) }) {
                    Icon(painter = painterResource(R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp", tint = WhatsAppGreen)
                }
            }
            IconButton(onClick = onEdit)   { Icon(Icons.Default.Edit, null, tint = Primary80) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = StatusLowStock) }
        }
    }
}

@Composable
fun ClientDetailScreen(client: Client, viewModel: ClientViewModel, onBack: () -> Unit) {
    val context      = LocalContext.current
    val equipment    by viewModel.getEquipmentForClient(client.id).collectAsState(initial = emptyList())
    val services     by viewModel.getServicesForClient(client.id).collectAsState(initial = emptyList())
    val appointments by viewModel.getAppointmentsForClient(client.id).collectAsState(initial = emptyList())
    val invoices     by viewModel.getInvoicesForClient(client.id).collectAsState(initial = emptyList())

    var showWhatsAppMenu by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    val sdfShort = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Fallas recurrentes: agrupar descripciones de servicios y contar repeticiones
    val recurringFaults = remember(services) {
        services.filter { it.description.isNotBlank() }
            .groupBy { it.description.lowercase().trim() }
            .filter { it.value.size > 1 }
            .map { (desc, list) -> Pair(list.first().description, list.size) }
            .sortedByDescending { it.second }
            .take(5)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text("${client.name}${if (client.lastName.isNotBlank()) " ${client.lastName}" else ""}",
                            fontWeight = FontWeight.Bold)
                        Text("${services.size} servicio(s) • ${equipment.size} equipo(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (client.phone.isNotBlank()) {
                        Box {
                            IconButton(onClick = { showWhatsAppMenu = true }) {
                                Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp", tint = WhatsAppGreen)
                            }
                            DropdownMenu(
                                expanded = showWhatsAppMenu,
                                onDismissRequest = { showWhatsAppMenu = false }
                            ) {
                                Text("WhatsApp rápido", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider()
                                listOf(
                                    "Voy en camino, nos vemos pronto.",
                                    "Confirmando nuestra cita. Estaré puntual.",
                                    "Su servicio ha sido completado satisfactoriamente. Gracias por su preferencia.",
                                    "Le recuerdo que tiene un mantenimiento programado próximamente.",
                                    ""
                                ).forEach { msg ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(if (msg.isEmpty()) "Escribir mensaje..." else msg,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2)
                                        },
                                        leadingIcon = {
                                            if (msg.isEmpty()) {
                                                Icon(Icons.Default.Edit, null, tint = WhatsAppGreen,
                                                    modifier = Modifier.size(16.dp))
                                            } else {
                                                Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                                    contentDescription = null, tint = WhatsAppGreen,
                                                    modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = { showWhatsAppMenu = false; openWhatsApp(context, client.phone, msg) }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Info básica ────────────────────────────────────────────────────
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Información del Cliente", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (client.clientType == "Persona Jurídica") Icons.Default.Business
                                else Icons.Default.Person,
                                null, tint = Primary80, modifier = Modifier.size(18.dp))
                            Text(client.clientType, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium, color = Primary80)
                        }
                        if (client.phone.isNotBlank())   InfoRow(Icons.Default.Phone, client.phone)
                        if (client.email.isNotBlank())   InfoRow(Icons.Default.Email, client.email)
                        if (client.address.isNotBlank()) InfoRow(Icons.Default.LocationOn, client.address)
                        if (client.notes.isNotBlank())   InfoRow(Icons.Default.Notes, client.notes)
                        // Acceso rápido WhatsApp
                        if (client.phone.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { openWhatsApp(context, client.phone) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Abrir en WhatsApp")
                            }
                        }
                    }
                }
            }

            // ── Fallas recurrentes ─────────────────────────────────────────────
            if (recurringFaults.isNotEmpty()) {
                item {
                    Text("Fallas recurrentes", fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleSmall)
                }
                items(recurringFaults) { (fault, count) ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = StatusLowStock.copy(alpha = 0.08f))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Warning, null, tint = StatusLowStock, modifier = Modifier.size(18.dp))
                            Text(fault, modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium)
                            Surface(shape = RoundedCornerShape(12.dp),
                                color = StatusLowStock.copy(alpha = 0.2f)) {
                                Text("×$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall, color = StatusLowStock,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Equipos ────────────────────────────────────────────────────────
            item {
                Text("Equipos (${equipment.size})", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
            }
            if (equipment.isEmpty()) {
                item {
                    Text("Sin equipos registrados.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(equipment) { eq ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Air, null, tint = Primary80)
                        Column {
                            Text("${eq.brand} ${eq.model}".trim(), fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.bodyMedium)
                            if (eq.serial.isNotBlank())
                                Text("Serie: ${eq.serial}", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Historial de Servicios ─────────────────────────────────────────
            item {
                Text("Historial de Servicios (${services.size})", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
            }
            if (services.isEmpty()) {
                item {
                    Text("Sin servicios registrados.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(services) { svc -> ServiceHistoryCard(svc, sdfShort) }

            // ── Citas ──────────────────────────────────────────────────────────
            item {
                Text("Citas (${appointments.size})", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
            }
            if (appointments.isEmpty()) {
                item {
                    Text("Sin citas registradas.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(appointments) { appt -> AppointmentHistoryCard(appt, sdf) }

            // ── Facturas ───────────────────────────────────────────────────────
            item {
                Text("Facturas (${invoices.size})", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleSmall)
            }
            if (invoices.isEmpty()) {
                item {
                    Text("Sin facturas generadas.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(invoices) { inv -> InvoiceHistoryCard(inv, sdf) }
        }
    }
}

@Composable
private fun ServiceHistoryCard(svc: ServiceOrder, sdf: SimpleDateFormat) {
    val (statusColor, statusLabel) = when (svc.status) {
        ServiceStatus.PENDING     -> StatusPending    to "Pendiente"
        ServiceStatus.IN_PROGRESS -> StatusInRepair   to "En Proceso"
        ServiceStatus.COMPLETED   -> StatusCompleted  to "Completado"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Build, null, tint = Primary80, modifier = Modifier.size(18.dp))
                Text(svc.description, modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tipo: ${when(svc.type) {
                    ServiceType.MAINTENANCE -> "Mantenimiento"
                    ServiceType.REPAIR -> "Reparación"
                    ServiceType.INSTALLATION -> "Instalación"
                }}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (svc.equipmentType.isNotBlank())
                    Text("Equipo: ${svc.equipmentType}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (svc.diagnosis.isNotBlank())
                Text("Diagnóstico: ${svc.diagnosis}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (svc.totalCost > 0.0)
                Text("Costo: \$${String.format("%.2f", svc.totalCost)}",
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
            Text("Creado: ${sdf.format(Date(svc.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            svc.completedAt?.let {
                Text("Completado: ${sdf.format(Date(it))}", style = MaterialTheme.typography.labelSmall,
                    color = StatusCompleted)
            }
        }
    }
}

@Composable
private fun AppointmentHistoryCard(appt: Appointment, sdf: SimpleDateFormat) {
    val (statusColor, statusLabel) = when (appt.status) {
        AppointmentStatus.SCHEDULED   -> StatusPending    to "Programada"
        AppointmentStatus.IN_PROGRESS -> StatusInRepair   to "En Proceso"
        AppointmentStatus.COMPLETED   -> StatusCompleted  to "Completada"
        AppointmentStatus.CANCELLED   -> Color(0xFFB71C1C) to "Cancelada"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CalendarToday, null, tint = Primary80, modifier = Modifier.size(18.dp))
                Text(sdf.format(Date(appt.dateTime)), modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tipo: ${when(appt.serviceType) {
                    ServiceType.MAINTENANCE -> "Mantenimiento"
                    ServiceType.REPAIR -> "Reparación"
                    ServiceType.INSTALLATION -> "Instalación"
                }}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (appt.equipmentType.isNotBlank())
                    Text("Equipo: ${appt.equipmentType}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InvoiceHistoryCard(inv: Invoice, sdf: SimpleDateFormat) {
    val (pColor, pLabel) = when (inv.paymentStatus) {
        com.gestion.itinerario.data.entity.PaymentStatus.PAID    -> StatusCompleted to "Pagado"
        com.gestion.itinerario.data.entity.PaymentStatus.PENDING -> Color(0xFFE65100) to "Pendiente"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "Sin estado"
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Receipt, null, tint = Primary80, modifier = Modifier.size(18.dp))
                Text(inv.invoiceNumber, modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                Surface(shape = RoundedCornerShape(6.dp), color = pColor.copy(alpha = 0.15f)) {
                    Text(pLabel, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, color = pColor)
                }
            }
            Text("\$${String.format("%.2f", inv.totalAmount)}",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary80)
            Text(sdf.format(java.util.Date(inv.createdAt)),
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (inv.equipmentType.isNotBlank())
                Text("Equipo: ${inv.equipmentType}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Primary80, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Formulario de cliente con validaciones completas
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(initial: Client?, onDismiss: () -> Unit, onSave: (Client) -> Unit) {
    val isJuridica = initial?.clientType == "Persona Jurídica"

    var name       by remember { mutableStateOf(initial?.name ?: "") }
    var lastName   by remember { mutableStateOf(initial?.lastName ?: "") }
    var phoneCode  by remember { mutableStateOf(
        // Al editar: extraer código del teléfono guardado
        PHONE_CODES.firstOrNull { initial?.phone?.startsWith(it) == true } ?: PHONE_CODES[0]
    ) }
    var phoneNumber by remember { mutableStateOf(
        // Al editar: extraer los 7 dígitos sin el código
        initial?.phone?.let { full ->
            val code = PHONE_CODES.firstOrNull { full.startsWith(it) }
            if (code != null) full.removePrefix(code).trimStart('-') else full
        } ?: ""
    ) }
    var email      by remember { mutableStateOf(initial?.email ?: "") }
    var address    by remember { mutableStateOf(initial?.address ?: "") }
    var notes      by remember { mutableStateOf(initial?.notes ?: "") }
    var clientType by remember { mutableStateOf(initial?.clientType ?: "Persona Natural") }

    // Estado del dropdown de código de área
    var codeDropdownExpanded by remember { mutableStateOf(false) }

    // Mensaje de error de validación
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isJuridicaMode = clientType == "Persona Jurídica"

    // ── Condición habilitación del botón Guardar ──────────────────────────────
    // Natural: nombre + apellido + teléfono (7 dígitos) + dirección requeridos
    // Jurídica: nombre empresa + teléfono (7 dígitos) + dirección requeridos
    val saveEnabled = if (isJuridicaMode) {
        name.isNotBlank() && phoneNumber.length == 7 && address.isNotBlank()
    } else {
        name.isNotBlank() && lastName.isNotBlank() && phoneNumber.length == 7 && address.isNotBlank()
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            icon  = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Error de validación", fontWeight = FontWeight.Bold) },
            text  = { Text(errorMessage!!) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) { Text("Entendido") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "Nuevo Cliente" else "Editar Cliente",
                fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Tipo de cliente ──────────────────────────────────────────
                Text("Tipo de cliente:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CLIENT_TYPES.forEach { tipo ->
                        FilterChip(
                            selected = clientType == tipo,
                            onClick  = {
                                clientType = tipo
                                // Al cambiar a jurídica limpiar apellido
                                if (tipo == "Persona Jurídica") lastName = ""
                            },
                            label    = { Text(tipo, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = if (clientType == tipo) {{
                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                            }} else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // ── Nombre (condicional por tipo de cliente) ─────────────────
                if (isJuridicaMode) {
                    // Persona Jurídica: un solo campo a todo el ancho, sin restricciones
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la empresa *") },
                        placeholder = { Text("Nombre de la empresa") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Business, null) }
                    )
                } else {
                    // Persona Natural: dos campos en fila — Nombre y Apellido
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { v ->
                                // Solo letras, espacios y caracteres especiales (sin dígitos)
                                if (!v.any { it.isDigit() }) name = v
                            },
                            label = { Text("Nombre *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { v ->
                                if (!v.any { it.isDigit() }) lastName = v
                            },
                            label = { Text("Apellido *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    // Hint sutil para el usuario
                    Text(
                        "El nombre y apellido no pueden contener números",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                // ── Teléfono: dropdown de código + 7 dígitos ─────────────────
                Text("Teléfono *", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dropdown código de área
                    ExposedDropdownMenuBox(
                        expanded = codeDropdownExpanded,
                        onExpandedChange = { codeDropdownExpanded = !codeDropdownExpanded },
                        modifier = Modifier.width(120.dp)
                    ) {
                        OutlinedTextField(
                            value = phoneCode,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Código") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeDropdownExpanded)
                            },
                            modifier = Modifier.menuAnchor(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = codeDropdownExpanded,
                            onDismissRequest = { codeDropdownExpanded = false }
                        ) {
                            PHONE_CODES.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    onClick = {
                                        phoneCode = code
                                        codeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Campo de 7 dígitos numéricos
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { v ->
                            // Solo dígitos, máximo 7
                            val digits = v.filter { it.isDigit() }
                            if (digits.length <= 7) phoneNumber = digits
                        },
                        label = { Text("7 dígitos") },
                        placeholder = { Text("1234567") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = phoneNumber.isNotEmpty() && phoneNumber.length < 7,
                        supportingText = {
                            Text("${phoneNumber.length}/7",
                                color = if (phoneNumber.length == 7)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )
                }

                // ── Correo y Dirección (sin restricciones) ───────────────────
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Dirección *") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Notas adicionales") },
                    modifier = Modifier.fillMaxWidth(), minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // ── Validaciones al guardar ───────────────────────────────
                    if (!isJuridicaMode) {
                        if (name.any { it.isDigit() }) {
                            errorMessage = "El nombre no puede contener números."
                            return@Button
                        }
                        if (lastName.any { it.isDigit() }) {
                            errorMessage = "El apellido no puede contener números."
                            return@Button
                        }
                    }
                    if (phoneNumber.length != 7) {
                        errorMessage = "El número de teléfono debe tener exactamente 7 dígitos."
                        return@Button
                    }
                    val fullPhone = "$phoneCode$phoneNumber"
                    onSave(Client(
                        name       = name.trim(),
                        lastName   = if (isJuridicaMode) "" else lastName.trim(),
                        phone      = fullPhone,
                        email      = email.trim(),
                        address    = address.trim(),
                        notes      = notes.trim(),
                        clientType = clientType
                    ))
                },
                enabled = saveEnabled
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
