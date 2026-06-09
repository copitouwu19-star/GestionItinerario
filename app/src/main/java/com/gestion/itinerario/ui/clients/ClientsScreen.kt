package com.gestion.itinerario.ui.clients

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
                        ) + 72.dp
                    )
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                    value = search, onValueChange = viewModel::onSearch,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Buscar por nombre o teléfono…") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
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
                        itemsIndexed(clients, key = { _, c -> c.id }) { index, c ->
                            ClientCard(c, accentIndex = index,
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
fun ClientCard(c: Client, accentIndex: Int = 0, onView: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val accentColors = listOf(Primary40, Secondary40, Tertiary40)
    val accentColor = accentColors[accentIndex % accentColors.size]
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(18.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.White),
        onClick  = onView
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(end = 130.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                        Text(
                            c.name + if (c.lastName.isNotBlank()) " ${c.lastName}" else "",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(2.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (c.clientType == "Persona Jurídica")
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
                        if (c.phone.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Phone, null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp))
                                Text(c.phone, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (c.address.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Row(
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
                    }
                }
                // ── Badges de acción (esquina superior derecha) ───────────────
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (c.phone.isNotBlank()) {
                        ActionBadge(onClick = { openWhatsApp(context, c.phone) }, containerColor = WhatsAppGreen.copy(alpha = 0.15f)) {
                            Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                contentDescription = "WhatsApp", tint = WhatsAppGreen, modifier = Modifier.size(20.dp))
                        }
                    }
                    ActionBadge(onClick = onEdit, containerColor = Primary80.copy(alpha = 0.15f)) {
                        Icon(Icons.Default.Edit, null, tint = Primary80, modifier = Modifier.size(20.dp))
                    }
                    ActionBadge(onClick = onDelete, containerColor = StatusLowStock.copy(alpha = 0.15f)) {
                        Icon(Icons.Default.Delete, null, tint = StatusLowStock, modifier = Modifier.size(20.dp))
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
        modifier = Modifier.size(40.dp)
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
    val equipment      by viewModel.getEquipmentForClient(client.id).collectAsState(initial = emptyList())
    val services       by viewModel.getServicesForClient(client.id).collectAsState(initial = emptyList())
    val invoices       by viewModel.getInvoicesForClient(client.id).collectAsState(initial = emptyList())
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
                            "${services.size} SERVICIOS · ${equipment.size} EQUIPOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            if (client.phone.isNotBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Abrir WhatsApp") },
                                    leadingIcon = {
                                        Icon(painter = painterResource(R.drawable.ic_whatsapp),
                                            contentDescription = null, tint = WhatsAppGreen,
                                            modifier = Modifier.size(18.dp))
                                    },
                                    onClick = { showMoreMenu = false; openWhatsApp(context, client.phone) }
                                )
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
                    shape = RoundedCornerShape(18.dp),
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
                                    shape = RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
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

            // ── Estadísticas: Equipos + Facturas ──────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTileCard(
                        modifier = Modifier.weight(1f),
                        count = equipment.size,
                        label = "EQUIPOS",
                        accentColor = Primary40
                    )
                    StatTileCard(
                        modifier = Modifier.weight(1f),
                        count = invoices.size,
                        label = "FACTURAS",
                        accentColor = Secondary40
                    )
                }
            }

            // ── Historial de Servicios ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Historial de Servicios (${services.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (services.isEmpty()) {
                item { EmptyState(icon = Icons.Default.BuildCircle, message = "Sin servicios registrados") }
            } else {
                itemsIndexed(services, key = { _, s -> s.id }) { index, svc ->
                    ServiceHistoryCard(svc = svc, sdf = sdf, accentColor = accentColors[index % accentColors.size])
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
                    .background(accentColor, RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
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
                if (inv.equipmentType.isNotBlank())
                    Text("Equipo: ${inv.equipmentType}", style = MaterialTheme.typography.bodySmall,
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
                Text("Tipo de cliente:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CLIENT_TYPES.forEach { tipo ->
                        FilterChip(
                            selected = clientType == tipo,
                            onClick  = { clientType = tipo; if (tipo == "Persona Jurídica") lastName = "" },
                            label    = { Text(tipo, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = if (clientType == tipo) {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                if (isJuridicaMode) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Nombre de la empresa *") },
                        placeholder = { Text("Nombre de la empresa") },
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Business, null) }
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { v -> if (!v.any { it.isDigit() }) name = v },
                            label = { Text("Nombre *") },
                            modifier = Modifier.weight(1f), singleLine = true
                        )
                        OutlinedTextField(
                            value = lastName,
                            onValueChange = { v -> if (!v.any { it.isDigit() }) lastName = v },
                            label = { Text("Apellido *") },
                            modifier = Modifier.weight(1f), singleLine = true
                        )
                    }
                    Text("El nombre y apellido no pueden contener números",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }

                Text("Teléfono *", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    ExposedDropdownMenuBox(
                        expanded = codeDropdownExpanded,
                        onExpandedChange = { codeDropdownExpanded = !codeDropdownExpanded },
                        modifier = Modifier.width(120.dp)
                    ) {
                        OutlinedTextField(
                            value = phoneCode, onValueChange = {}, readOnly = true,
                            label = { Text("Código") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = codeDropdownExpanded) },
                            modifier = Modifier.menuAnchor(), singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = codeDropdownExpanded,
                            onDismissRequest = { codeDropdownExpanded = false }
                        ) {
                            PHONE_CODES.forEach { code ->
                                DropdownMenuItem(
                                    text = { Text(code) },
                                    onClick = { phoneCode = code; codeDropdownExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { v -> val d = v.filter { it.isDigit() }; if (d.length <= 7) phoneNumber = d },
                        label = { Text("7 dígitos") },
                        placeholder = { Text("1234567") },
                        modifier = Modifier.weight(1f), singleLine = true,
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
                    if (!isJuridicaMode) {
                        if (name.any { it.isDigit() }) { errorMessage = "El nombre no puede contener números."; return@Button }
                        if (lastName.any { it.isDigit() }) { errorMessage = "El apellido no puede contener números."; return@Button }
                    }
                    if (phoneNumber.length != 7) { errorMessage = "El número de teléfono debe tener exactamente 7 dígitos."; return@Button }
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
