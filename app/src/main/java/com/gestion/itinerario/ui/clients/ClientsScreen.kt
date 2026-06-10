package com.gestion.itinerario.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.sp
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
import com.gestion.itinerario.ui.theme.Secondary40
import com.gestion.itinerario.ui.theme.Secondary80
import kotlinx.coroutines.launch
import com.gestion.itinerario.ui.theme.Tertiary40
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
    onNavigateToProfile: () -> Unit = {},
    viewModel: ClientViewModel = hiltViewModel()
) {
    val clients    by viewModel.clients.collectAsStateWithLifecycle()
    val search     by viewModel.search.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var editClient by remember { mutableStateOf<Client?>(null) }
    var detailClient by remember { mutableStateOf<Client?>(null) }
    var confirmDeleteClient by remember { mutableStateOf<Client?>(null) }

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
                TopAppBar(
                    title = { Text("Clientes", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Perfil",
                                tint = Primary80, modifier = Modifier.size(28.dp))
                        }
                    }
                )
            }
        ) { scaffoldPadding ->
            Column(
                modifier = Modifier
                    .padding(top = scaffoldPadding.calculateTopPadding())
                    .fillMaxSize()
            ) {
                // Barra de búsqueda blanca con sombra
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(50),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    OutlinedTextField(
                        value = search, onValueChange = viewModel::onSearch,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Buscar por nombre o teléfono…",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent
                        )
                    )
                }
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
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 4.dp,
                            bottom = innerPadding.calculateBottomPadding() + 88.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(clients, key = { _, c -> c.id }) { _, c ->
                            ClientCard(c,
                                onView   = { detailClient = c },
                                onEdit   = { editClient = c; showDialog = true },
                                onDelete = { confirmDeleteClient = c })
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { editClient = null; showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = innerPadding.calculateBottomPadding() + 16.dp),
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

// ─── Tarjeta de cliente ───────────────────────────────────────────────────────
@Composable
fun ClientCard(c: Client, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val isJuridica = c.clientType == "Persona Jurídica"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(22.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        onClick  = onView
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Borde izquierdo degradado
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            if (isJuridica) listOf(Secondary40, Primary40) else listOf(Primary40, Secondary40)
                        ),
                        shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
                    )
            )
            // Cuerpo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, top = 12.dp, bottom = 10.dp, end = 10.dp)
            ) {
                // Nombre + badges en fila horizontal (esquina superior derecha)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        c.name + if (c.lastName.isNotBlank()) " ${c.lastName}" else "",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (c.phone.isNotBlank()) {
                            ActionBadge(
                                onClick = { openWhatsApp(context, c.phone) },
                                containerColor = WhatsAppGreen.copy(alpha = 0.14f)
                            ) {
                                Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp", tint = WhatsAppGreen,
                                    modifier = Modifier.size(18.dp))
                            }
                        }
                        ActionBadge(onClick = onEdit, containerColor = Color(0xFF2196F3).copy(alpha = 0.12f)) {
                            Icon(Icons.Default.Edit, null, tint = Color(0xFF2196F3), modifier = Modifier.size(18.dp))
                        }
                        ActionBadge(onClick = onDelete, containerColor = StatusLowStock.copy(alpha = 0.12f)) {
                            Icon(Icons.Default.Delete, null, tint = StatusLowStock, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Chip tipo cliente
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isJuridica) Secondary80.copy(alpha = 0.15f) else Primary80.copy(alpha = 0.12f)
                ) {
                    Text(
                        c.clientType,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isJuridica) Secondary40 else Primary40
                    )
                }
                // Divisor sutil
                HorizontalDivider(
                    modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    thickness = 0.8.dp
                )
                // Teléfono
                if (c.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Icon(Icons.Default.Phone, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp))
                        Text(c.phone, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // Dirección
                if (c.address.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp))
                        Text(c.address, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBadge(onClick: () -> Unit, containerColor: Color, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.size(36.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

// ─── Cabecera de sección estilo referencia ────────────────────────────────────
@Composable
private fun SectionHeader(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

// ─── Pantalla de detalle del cliente ─────────────────────────────────────────
@Composable
fun ClientDetailScreen(
    client: Client,
    viewModel: ClientViewModel,
    onBack: () -> Unit,
    invoiceViewModel: com.gestion.itinerario.ui.invoice.InvoiceViewModel = hiltViewModel(),
    profileViewModel: com.gestion.itinerario.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val appointmentsRaw by viewModel.getAppointmentsForClient(client.id).collectAsState(initial = emptyList())
    val appointments = remember(appointmentsRaw) { appointmentsRaw.sortedByDescending { it.dateTime } }
    val invoices       by viewModel.getInvoicesForClient(client).collectAsState(initial = emptyList())
    val companyProfile by profileViewModel.profile.collectAsStateWithLifecycle()
    val pdfScope = rememberCoroutineScope()
    var generatingInvoiceId by remember { mutableStateOf<String?>(null) }
    var showMoreMenu        by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }

    val accentColors = listOf(Primary40, Secondary40, Tertiary40)

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "${client.name}${if (client.lastName.isNotBlank()) " ${client.lastName}" else ""}",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${appointments.size} SERVICIOS · ${invoices.size} FACTURAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (client.phone.isNotBlank()) {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_whatsapp),
                                    contentDescription = "WhatsApp",
                                    tint = WhatsAppGreen
                                )
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                Text(
                                    "Mensaje rápido",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                HorizontalDivider()
                                listOf(
                                    "Voy en camino, nos vemos pronto.",
                                    "Confirmando nuestra cita. Estaré puntual.",
                                    "Su servicio ha sido completado. ¡Gracias por su preferencia!",
                                    "Le recuerdo que tiene un mantenimiento próximamente.",
                                    ""
                                ).forEach { msg ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (msg.isEmpty()) "Escribir mensaje..." else msg,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 2
                                            )
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
                                        onClick = {
                                            showMoreMenu = false
                                            openWhatsApp(context, client.phone, msg)
                                        }
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Tarjeta de información del cliente ────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        // Borde izquierdo degradado morado→rosa como en la referencia
                        Box(
                            modifier = Modifier
                                .width(5.dp)
                                .fillMaxHeight()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(Primary40, Secondary40)
                                    ),
                                    shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp)
                                )
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "INFORMACIÓN DEL CLIENTE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            InfoRow(Icons.Default.Person,      client.clientType)
                            if (client.phone.isNotBlank())
                                InfoRow(Icons.Default.Phone,       client.phone)
                            if (client.address.isNotBlank())
                                InfoRow(Icons.Default.LocationOn,  client.address)
                            if (client.email.isNotBlank())
                                InfoRow(Icons.Default.Email,       client.email)
                            if (client.notes.isNotBlank())
                                InfoRow(Icons.Default.StickyNote2, client.notes)
                            if (client.phone.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Button(
                                    onClick = { openWhatsApp(context, client.phone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                        contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Abrir en WhatsApp", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Estadísticas: Servicios (= Citas) ─────────────────────────────
            item {
                StatTileCard(
                    modifier = Modifier.fillMaxWidth(),
                    count = appointments.size,
                    label = "SERVICIOS",
                    accentColor = Primary40
                )
            }

            // ── Historial de Servicios ─────────────────────────────────────────
            item {
                Text(
                    "Historial de Servicios (${appointments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (appointments.isEmpty()) {
                item { EmptyState(icon = Icons.Default.CalendarToday, message = "Sin servicios registrados") }
            } else {
                itemsIndexed(appointments, key = { _, a -> a.id }) { index, appt ->
                    AppointmentHistoryCard(appt = appt, sdf = sdf, accentColor = accentColors[index % accentColors.size])
                }
            }

            // ── Facturas ───────────────────────────────────────────────────────
            if (invoices.isEmpty()) {
                item { EmptyState(icon = Icons.Default.Receipt, message = "Sin facturas generadas") }
            } else {
                item {
                    Text(
                        "Facturas (${invoices.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                itemsIndexed(invoices, key = { _, inv -> inv.id }) { index, inv ->
                    InvoiceHistoryCard(
                        inv = inv,
                        sdf = sdf,
                        accentColor = accentColors[index % accentColors.size],
                        isLoadingPdf = generatingInvoiceId == inv.id,
                        onViewPdf = {
                            generatingInvoiceId = inv.id
                            pdfScope.launch {
                                try {
                                    val file = invoiceViewModel.generatePdf(context, inv, companyProfile)
                                    com.gestion.itinerario.ui.invoice.InvoicePdfGenerator.openPdf(context, file)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "No se pudo generar el PDF.", android.widget.Toast.LENGTH_LONG).show()
                                }
                                generatingInvoiceId = null
                            }
                        }
                    )
                }
            }
        }
    }
}

// ── Tile de estadística (Equipos / Facturas) ──────────────────────────────────
@Composable
private fun StatTileCard(modifier: Modifier, count: Int, label: String, accentColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor, RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            )
        }
    }
}

// ── Estado vacío ──────────────────────────────────────────────────────────────
@Composable
private fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(40.dp))
        Text(message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Tarjeta historial servicio ───────────────────────────────────────────────
@Composable
private fun ServiceHistoryCard(svc: ServiceOrder, sdf: SimpleDateFormat, accentColor: Color = Primary40) {
    val (statusColor, statusLabel) = when (svc.status) {
        ServiceStatus.PENDING     -> StatusPending   to "PENDIENTE"
        ServiceStatus.IN_PROGRESS -> StatusInRepair  to "EN PROCESO"
        ServiceStatus.COMPLETED   -> StatusCompleted to "COMPLETADA"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp))
                    Text(
                        sdf.format(Date(svc.createdAt)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.14f)
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
                Text(
                    "Tipo: ${when(svc.type) {
                        ServiceType.MAINTENANCE  -> "Mantenimiento"
                        ServiceType.REPAIR       -> "Reparación"
                        ServiceType.INSTALLATION -> "Instalación"
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (svc.equipmentType.isNotBlank())
                    Text("Equipo: ${svc.equipmentType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (svc.description.isNotBlank())
                    Text(svc.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (svc.totalCost > 0.0)
                    Text("\$${String.format("%.2f", svc.totalCost)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary)
                svc.warrantyExpiresAt?.let { expiresAt ->
                    val vigente = expiresAt > System.currentTimeMillis()
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, null,
                            tint = if (vigente) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp))
                        Text(
                            if (vigente) "En garantía hasta ${sdf.format(Date(expiresAt))}"
                            else "Garantía vencida",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (vigente) StatusCompleted else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// ─── Tarjeta historial cita ───────────────────────────────────────────────────
@Composable
private fun AppointmentHistoryCard(appt: Appointment, sdf: SimpleDateFormat, accentColor: Color = Tertiary40) {
    val (statusColor, statusLabel) = when (appt.status) {
        AppointmentStatus.SCHEDULED   -> StatusPending   to "PROGRAMADA"
        AppointmentStatus.IN_PROGRESS -> StatusInRepair  to "EN PROCESO"
        AppointmentStatus.COMPLETED   -> StatusCompleted to "COMPLETADA"
        AppointmentStatus.CANCELLED   -> StatusLowStock  to "CANCELADA"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp))
                    Text(
                        sdf.format(Date(appt.dateTime)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.14f)
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
                Text(
                    "Tipo: ${when (appt.serviceType) {
                        ServiceType.MAINTENANCE  -> "Mantenimiento"
                        ServiceType.REPAIR       -> "Reparación"
                        ServiceType.INSTALLATION -> "Instalación"
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (appt.equipmentType.isNotBlank())
                    Text("Equipo: ${appt.equipmentType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (appt.notes.isNotBlank())
                    Text(appt.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ─── Tarjeta historial factura ────────────────────────────────────────────────
@Composable
private fun InvoiceHistoryCard(
    inv: Invoice,
    sdf: SimpleDateFormat,
    accentColor: Color = Primary40,
    isLoadingPdf: Boolean,
    onViewPdf: () -> Unit
) {
    val (pColor, pLabel) = when (inv.paymentStatus) {
        com.gestion.itinerario.data.entity.PaymentStatus.PAID    -> StatusCompleted to "PAGADO"
        com.gestion.itinerario.data.entity.PaymentStatus.PENDING -> Color(0xFFE65100) to "PENDIENTE"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "SIN ESTADO"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            )
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Receipt, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp))
                    Text(inv.invoiceNumber, modifier = Modifier.weight(1f),
                        fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Surface(shape = RoundedCornerShape(6.dp), color = pColor.copy(alpha = 0.14f)) {
                        Text(pLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            color = pColor)
                    }
                }
                Text("\$${String.format("%.2f", inv.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Text(sdf.format(java.util.Date(inv.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                OutlinedButton(
                    onClick = onViewPdf, enabled = !isLoadingPdf,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (isLoadingPdf) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Generando…", style = MaterialTheme.typography.labelSmall)
                    } else {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp), tint = Primary80)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver PDF", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
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

// ─── Formulario de cliente ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormDialog(initial: Client?, onDismiss: () -> Unit, onSave: (Client) -> Unit) {
    var name        by remember { mutableStateOf(initial?.name ?: "") }
    var lastName    by remember { mutableStateOf(initial?.lastName ?: "") }
    var phoneCode   by remember { mutableStateOf(
        PHONE_CODES.firstOrNull { initial?.phone?.startsWith(it) == true } ?: PHONE_CODES[0]
    ) }
    var phoneNumber by remember { mutableStateOf(
        initial?.phone?.let { full ->
            val code = PHONE_CODES.firstOrNull { full.startsWith(it) }
            if (code != null) full.removePrefix(code).trimStart('-') else full
        } ?: ""
    ) }
    var email      by remember { mutableStateOf(initial?.email ?: "") }
    var address    by remember { mutableStateOf(initial?.address ?: "") }
    var notes      by remember { mutableStateOf(initial?.notes ?: "") }
    var clientType by remember { mutableStateOf(initial?.clientType ?: "Persona Natural") }
    var codeDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isJuridicaMode = clientType == "Persona Jurídica"

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
            confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("Entendido") } }
        )
    }

    fun doSave() {
        if (!isJuridicaMode) {
            if (name.any { it.isDigit() }) { errorMessage = "El nombre no puede contener números."; return }
            if (lastName.any { it.isDigit() }) { errorMessage = "El apellido no puede contener números."; return }
        }
        if (phoneNumber.length != 7) { errorMessage = "El número de teléfono debe tener exactamente 7 dígitos."; return }
        onSave(Client(
            name       = name.trim(),
            lastName   = if (isJuridicaMode) "" else lastName.trim(),
            phone      = "$phoneCode$phoneNumber",
            email      = email.trim(),
            address    = address.trim(),
            notes      = notes.trim(),
            clientType = clientType
        ))
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        title = { Text(if (initial == null) "Nuevo Cliente" else "Editar Cliente", fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Default.ArrowBack, null) } },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                    )
                },
                bottomBar = {
                    Surface(shadowElevation = 8.dp, color = Color.White) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss, modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50)
                            ) { Text("Cancelar") }
                            Button(
                                onClick = { doSave() },
                                enabled = saveEnabled,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(
                                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                            ),
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Text("Guardar", color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
            ) { scaffoldPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // ── Banner ────────────────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary))
                            Row(modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text(
                                        if (initial == null) "Registrar Cliente" else "Editar Cliente",
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Complete los datos del cliente.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // ── Tipo de cliente ───────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TIPO DE CLIENTE", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            CLIENT_TYPES.forEach { tipo ->
                                val selected = clientType == tipo
                                Surface(
                                    onClick = { clientType = tipo; if (tipo == "Persona Jurídica") lastName = "" },
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) MaterialTheme.colorScheme.primary else Color.White,
                                    shadowElevation = if (selected) 0.dp else 2.dp,
                                    border = if (selected) null else
                                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center) {
                                        Icon(
                                            if (tipo == "Persona Jurídica") Icons.Default.Business else Icons.Default.Person,
                                            null, tint = if (selected) Color.White else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(tipo, style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    // ── Nombre ────────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (isJuridicaMode) "NOMBRE DE LA EMPRESA *" else "NOMBRE Y APELLIDO *",
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, letterSpacing = 1.sp)
                        if (isJuridicaMode) {
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = name, onValueChange = { name = it },
                                    placeholder = { Text("Nombre de la empresa") },
                                    leadingIcon = { Icon(Icons.Default.Business, null) },
                                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                    shadowElevation = 2.dp, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { v -> if (!v.any { it.isDigit() }) name = v },
                                        placeholder = { Text("Nombre") },
                                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                                Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                    shadowElevation = 2.dp, modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = lastName,
                                        onValueChange = { v -> if (!v.any { it.isDigit() }) lastName = v },
                                        placeholder = { Text("Apellido") },
                                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Teléfono ──────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TELÉFONO *", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = codeDropdownExpanded,
                                onExpandedChange = { codeDropdownExpanded = !codeDropdownExpanded },
                                modifier = Modifier.width(130.dp)
                            ) {
                                Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                    shadowElevation = 2.dp) {
                                    OutlinedTextField(
                                        value = phoneCode, onValueChange = {}, readOnly = true,
                                        placeholder = { Text("Código") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeDropdownExpanded) },
                                        modifier = Modifier.menuAnchor().fillMaxWidth(), singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                }
                                ExposedDropdownMenu(expanded = codeDropdownExpanded,
                                    onDismissRequest = { codeDropdownExpanded = false }) {
                                    PHONE_CODES.forEach { code ->
                                        DropdownMenuItem(text = { Text(code) },
                                            onClick = { phoneCode = code; codeDropdownExpanded = false })
                                    }
                                }
                            }
                            Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                                shadowElevation = 2.dp, modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { v -> val d = v.filter { it.isDigit() }; if (d.length <= 7) phoneNumber = d },
                                    placeholder = { Text("1234567") },
                                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    isError = phoneNumber.isNotEmpty() && phoneNumber.length < 7,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent),
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                        if (phoneNumber.isNotEmpty())
                            Text("${phoneNumber.length}/7 dígitos",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (phoneNumber.length == 7) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // ── Dirección ─────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("DIRECCIÓN *", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = address, onValueChange = { address = it },
                                placeholder = { Text("Ej. Caracas, Venezuela") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // ── Correo (opcional) ──────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("CORREO ELECTRÓNICO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = email, onValueChange = { email = it },
                                placeholder = { Text("correo@ejemplo.com") },
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                modifier = Modifier.fillMaxWidth(), singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }

                    // ── Notas adicionales ─────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("NOTAS ADICIONALES", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White,
                            shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = notes, onValueChange = { notes = it },
                                placeholder = { Text("Ej. El cliente solicita llamar antes de llegar…") },
                                modifier = Modifier.fillMaxWidth(), minLines = 3,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent),
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
