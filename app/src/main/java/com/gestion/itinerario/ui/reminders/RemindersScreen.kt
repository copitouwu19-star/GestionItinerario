package com.gestion.itinerario.ui.reminders

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.entity.PaymentMethod
import com.gestion.itinerario.data.entity.PaymentStatus
import com.gestion.itinerario.data.entity.REMINDER_SOURCE_AUTO
import com.gestion.itinerario.data.entity.REMINDER_SOURCE_MANUAL
import com.gestion.itinerario.ui.components.UserMenuIconButton
import com.gestion.itinerario.ui.invoice.InvoiceCreationDialog
import com.gestion.itinerario.ui.profile.ProfileViewModel
import com.gestion.itinerario.ui.services.MaintenanceDetailScreen
import com.gestion.itinerario.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

private data class IntervalOption(val value: Int, val unit: IntervalUnit, val label: String)

private val INTERVAL_OPTIONS = listOf(
    IntervalOption(3,  IntervalUnit.MONTHS, "3 meses"),
    IntervalOption(6,  IntervalUnit.MONTHS, "6 meses"),
    IntervalOption(12, IntervalUnit.MONTHS, "1 año")
)

private val EQUIPMENT_TYPES = listOf("Nevera", "Aire Acondicionado", "Lavadora", "Otro")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    innerPadding: PaddingValues = PaddingValues(),
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
    viewModel: ReminderViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val clients   by viewModel.clients.collectAsStateWithLifecycle()
    val companyProfile by profileViewModel.profile.collectAsStateWithLifecycle()
    val professionalName = companyProfile.ownerName.ifBlank { companyProfile.companyName }.ifBlank { "Profesional asignado" }
    var showDialog          by remember { mutableStateOf(false) }
    var editReminder        by remember { mutableStateOf<MaintenanceReminder?>(null) }
    var detailReminder      by remember { mutableStateOf<MaintenanceReminder?>(null) }
    var invoiceReminder     by remember { mutableStateOf<MaintenanceReminder?>(null) }
    var completedReminder   by remember { mutableStateOf<MaintenanceReminder?>(null) }
    var showPendingDetail   by remember { mutableStateOf(false) }
    var showMonthDetail     by remember { mutableStateOf(false) }
    var searchQuery     by remember { mutableStateOf("") }
    var filterToday     by remember { mutableStateOf(false) }

    val today       = System.currentTimeMillis()
    val todayEnd    = today + 86_400_000L
    val activeReminders = reminders.filter { it.workStatus != "COMPLETED" }
    val pendingCount    = activeReminders.count { it.nextServiceDate <= today }
    val thisMonthCount  = activeReminders.count { it.nextServiceDate in today..(today + 30 * 86_400_000L) }

    val filtered = activeReminders.filter { r ->
        val matchesSearch = searchQuery.isBlank() ||
            r.equipmentType.contains(searchQuery, ignoreCase = true) ||
            clients.find { it.id == r.clientId }
                ?.let { "${it.name} ${it.lastName}".contains(searchQuery, ignoreCase = true) } == true
        val matchesFilter = when {
            filterToday -> r.nextServiceDate in today..todayEnd
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = { Text("Mantenimiento", fontWeight = FontWeight.Bold) },
                actions = {
                    UserMenuIconButton(
                        onNavigateToProfile = onNavigateToProfile,
                        onLogout = onLogout
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Primary40,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        val bottomPad = maxOf(innerPadding.calculateBottomPadding(), padding.calculateBottomPadding())

        LazyColumn(
            modifier = Modifier
                .background(Color(0xFFF5F5F5))
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = 12.dp, bottom = bottomPad + 80.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Fila de estadísticas ──────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card PENDIENTES
                    Surface(
                        modifier = Modifier.weight(1f).clickable { showPendingDetail = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        tonalElevation = 0.dp,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Color(0xFFD32F2F))
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "PENDIENTES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    pendingCount.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    "Ver todos >",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary40
                                )
                            }
                        }
                    }
                    // Card ESTE MES
                    Surface(
                        modifier = Modifier.weight(1f).clickable { showMonthDetail = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        tonalElevation = 0.dp,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(Primary40)
                            )
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "ESTE MES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    thisMonthCount.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary40
                                )
                                Text(
                                    "Ver todos >",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary40
                                )
                            }
                        }
                    }
                }
            }

            // ── Barra de búsqueda + filtros ───────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Buscar equipo o cliente...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                    // Filtro HOY
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { filterToday = !filterToday },
                            shape = RoundedCornerShape(50),
                            color = if (filterToday) Primary40 else Color.White,
                            shadowElevation = if (filterToday) 0.dp else 2.dp
                        ) {
                            Text(
                                "HOY",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (filterToday) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ── Lista vacía ───────────────────────────────────────────────────
            if (filtered.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                if (reminders.isEmpty()) "Sin recordatorios de mantenimiento"
                                else "Sin resultados para la búsqueda",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (reminders.isEmpty()) {
                                Text(
                                    "Toca + para programar el próximo",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Tarjetas de recordatorios ─────────────────────────────────────
            items(filtered, key = { it.id }) { r ->
                val client = clients.find { it.id == r.clientId }
                ReminderCard(
                    r = r,
                    client = client,
                    onMarkDone = { viewModel.markDone(r) },
                    onDelete = { viewModel.delete(r) },
                    onEdit = { editReminder = r; showDialog = true },
                    onViewDetails = { detailReminder = r }
                )
            }
        }
    }

    if (showDialog) {
        ReminderFormDialog(
            clients = clients,
            initial = editReminder,
            onDismiss = { showDialog = false; editReminder = null },
            onSave = { r ->
                if (editReminder != null) viewModel.update(r.copy(id = editReminder!!.id))
                else viewModel.save(r)
                showDialog = false
                editReminder = null
            }
        )
    }

    detailReminder?.let { r ->
        val client = clients.find { it.id == r.clientId }
        val cn = client?.let { "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}" } ?: "Sin cliente"
        MaintenanceDetailScreen(
            reminder         = r,
            clientName       = cn,
            clientPhone      = client?.phone ?: "",
            professionalName = professionalName,
            onDismiss        = { detailReminder = null },
            onCompleted      = { completedR ->
                detailReminder = null
                invoiceReminder = completedR
            }
        )
    }

    // ── Factura al completar mantenimiento ────────────────────────────────────
    invoiceReminder?.let { r ->
        val client = clients.find { it.id == r.clientId }
        val cn = client?.let { "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}" } ?: "Sin cliente"
        val serviceDesc = "Mantenimiento preventivo${if (r.equipmentType.isNotBlank()) " – ${r.equipmentType}" else ""}"
        InvoiceCreationDialog(
            serviceOrderId     = r.id,
            clientId           = r.clientId,
            clientName         = cn,
            clientPhone        = client?.phone ?: "",
            clientAddress      = client?.address ?: "",
            clientType         = client?.clientType ?: "Persona Natural",
            equipmentType      = r.equipmentType,
            serviceDescription = serviceDesc,
            diagnosis          = r.notes,
            totalAmount        = 0.0,
            paymentMethod      = PaymentMethod.NONE,
            paymentStatus      = PaymentStatus.NONE,
            startDate          = if (r.nextServiceDate > 0L) r.nextServiceDate else System.currentTimeMillis(),
            onDismiss          = { invoiceReminder = null; completedReminder = r },
            onInvoiceCreated   = { _, _ -> invoiceReminder = null; completedReminder = r }
        )
    }

    // ── Próximo ciclo de mantenimiento ────────────────────────────────────────
    completedReminder?.let { r ->
        val sdf = remember { java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()) }
        val cal = remember(r) {
            java.util.Calendar.getInstance().apply {
                when (r.intervalUnit) {
                    IntervalUnit.WEEKS  -> add(java.util.Calendar.WEEK_OF_YEAR, r.intervalValue)
                    IntervalUnit.MONTHS -> add(java.util.Calendar.MONTH, r.intervalValue)
                }
            }
        }
        val intervalLabel = when (r.intervalUnit) {
            IntervalUnit.WEEKS  -> "cada ${r.intervalValue} semana(s)"
            IntervalUnit.MONTHS -> when (r.intervalValue) { 12 -> "cada 1 año"; else -> "cada ${r.intervalValue} mes(es)" }
        }
        AlertDialog(
            onDismissRequest = { completedReminder = null },
            containerColor = Color.White,
            tonalElevation = 0.dp,
            icon = { Icon(Icons.Default.NotificationsActive, null, tint = Primary40) },
            title = { Text("¿Programar próximo ciclo?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mantenimiento: ${r.equipmentType.ifBlank { "equipo" }} — $intervalLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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
                Button(onClick = { viewModel.markDone(r); completedReminder = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary40)) {
                    Text("Confirmar ciclo")
                }
            },
            dismissButton = {
                TextButton(onClick = { completedReminder = null }) { Text("Omitir") }
            }
        )
    }

    if (showPendingDetail) {
        ReminderListDialog(
            title = "Pendientes",
            reminders = activeReminders.filter { it.nextServiceDate <= today },
            clients = clients,
            onDismiss = { showPendingDetail = false }
        )
    }
    if (showMonthDetail) {
        ReminderListDialog(
            title = "Este mes",
            reminders = activeReminders.filter { it.nextServiceDate in today..(today + 30 * 86_400_000L) },
            clients = clients,
            onDismiss = { showMonthDetail = false }
        )
    }
}

// ─── Tarjeta de mantenimiento ─────────────────────────────────────────────────
@Composable
fun ReminderCard(
    r: MaintenanceReminder,
    client: Client?,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onViewDetails: () -> Unit
) {
    val sdf     = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val now      = System.currentTimeMillis()
    val isDue    = r.nextServiceDate <= now
    val daysLeft = ((r.nextServiceDate - now) / 86_400_000L).toInt()
    val isAuto   = r.source == REMINDER_SOURCE_AUTO

    // Colores según urgencia
    val accentColor = when {
        isDue || daysLeft <= 3          -> Color(0xFFD32F2F)
        daysLeft in 4..7                -> Color(0xFFFF8F00)
        daysLeft in 8..30               -> Primary40
        else                             -> Color(0xFF00897B)
    }

    // Ícono según tipo de equipo
    val equipIcon = when (r.equipmentType.lowercase()) {
        "nevera"             -> Icons.Default.AcUnit
        "aire acondicionado" -> Icons.Default.Air
        "lavadora"           -> Icons.Default.WaterDrop
        else                 -> Icons.Default.Build
    }

    val intervalText = when (r.intervalValue) {
        12   -> "Cada 1 año"
        else -> "Cada ${r.intervalValue} mes(es)"
    }

    val clientName = client?.let {
        "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}"
    } ?: "Sin cliente"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Borde izquierdo de color según urgencia
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accentColor, RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ── ROW principal: ícono + datos + badge/delete ────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Ícono del equipo en círculo
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            equipIcon, null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Datos del cliente y equipo
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            clientName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            r.equipmentType.ifBlank { "Equipo" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            intervalText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Badge días + papelera
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Badge urgencia
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accentColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                if (isDue) "VENCIDO" else "EN $daysLeft DÍAS",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                        // Botones editar + papelera
                        Row {
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit, null,
                                    tint = Primary40,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete, null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // ── Fechas servicio ────────────────────────────────────────
                if (isAuto && r.lastServiceDate > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "ÚLTIMO SERVICIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                sdf.format(Date(r.lastServiceDate)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "PRÓXIMO SERVICIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                sdf.format(Date(r.nextServiceDate)),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                if (isAuto) "PRÓXIMO SERVICIO" else "FECHA DEL SERVICIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                if (r.nextServiceDate > 0L) {
                                    if (isAuto) sdf.format(Date(r.nextServiceDate))
                                    else sdfFull.format(Date(r.nextServiceDate))
                                } else "Sin fecha",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = accentColor
                            )
                        }
                    }
                }

            }
        }
    }
}

// ─── Formulario de nuevo mantenimiento ────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFormDialog(
    clients: List<Client>,
    initial: MaintenanceReminder? = null,
    onDismiss: () -> Unit,
    onSave: (MaintenanceReminder) -> Unit
) {
    val context = LocalContext.current
    val dateSdf  = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeSdf  = SimpleDateFormat("HH:mm", Locale.getDefault())
    val calendar = remember {
        Calendar.getInstance().also { cal ->
            if (initial != null && initial.nextServiceDate > 0L) cal.timeInMillis = initial.nextServiceDate
        }
    }

    var dateStr  by remember { mutableStateOf(
        if (initial != null && initial.nextServiceDate > 0L) dateSdf.format(Date(initial.nextServiceDate)) else ""
    ) }
    var timeStr  by remember { mutableStateOf(
        if (initial != null && initial.nextServiceDate > 0L) timeSdf.format(Date(initial.nextServiceDate)) else ""
    ) }
    var notes    by remember { mutableStateOf(initial?.notes ?: "") }
    var equipmentType by remember { mutableStateOf(initial?.equipmentType ?: "") }
    var selected by remember { mutableStateOf(
        INTERVAL_OPTIONS.find { it.value == initial?.intervalValue } ?: INTERVAL_OPTIONS[0]
    ) }
    var selectedClient         by remember { mutableStateOf<Client?>(clients.find { it.id == initial?.clientId }) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    val equipmentIcons = mapOf(
        "Nevera"             to Icons.Default.AcUnit,
        "Aire Acondicionado" to Icons.Default.Air,
        "Lavadora"           to Icons.Default.WaterDrop,
        "Otro"               to Icons.Default.MoreHoriz
    )

    val timePicker = TimePickerDialog(context, { _, h, min ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
        timeStr = String.format("%02d:%02d", h, min)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        dateStr = String.format("%02d/%02d/%04d", d, m + 1, y)
        timePicker.show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(Primary40),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Engineering, null, tint = Color.White,
                            modifier = Modifier.size(24.dp))
                    }
                    Text(if (initial != null) "Editar Mantenimiento" else "Nuevo Mantenimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }

                HorizontalDivider()

                // ── Cliente
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("CLIENTE *", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth()
                                .clickable { clientDropdownExpanded = !clientDropdownExpanded },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    text = { Text("Sin clientes registrados",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                    onClick = { clientDropdownExpanded = false })
                            } else {
                                clients.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text("${c.name} ${c.lastName}".trim()) },
                                        onClick = { selectedClient = c; clientDropdownExpanded = false },
                                        leadingIcon = {
                                            Icon(Icons.Default.Person, null,
                                                tint = MaterialTheme.colorScheme.primary)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tipo de Equipo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("TIPO DE EQUIPO", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EQUIPMENT_TYPES.chunked(2).forEach { row ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                row.forEach { et ->
                                    val sel = equipmentType == et
                                    Surface(
                                        onClick = { equipmentType = if (equipmentType == et) "" else et },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (sel) MaterialTheme.colorScheme.primary.copy(0.10f) else Color.White,
                                        shadowElevation = if (sel) 0.dp else 2.dp,
                                        border = if (sel)
                                            androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                        else
                                            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier.size(30.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary.copy(if (sel) 0.18f else 0.10f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(equipmentIcons[et] ?: Icons.Default.Build, null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp))
                                            }
                                            Text(et,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                                                color = if (sel) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurface)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Fecha y Hora de la visita
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("FECHA Y HORA DE LA VISITA *", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp))
                            Text(
                                if (dateStr.isNotBlank() && timeStr.isNotBlank()) "$dateStr  $timeStr"
                                else "Seleccionar fecha y hora",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (dateStr.isNotBlank())
                                    MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(Icons.Default.EditCalendar, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // ── Intervalo siguiente mantenimiento
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("REPETIR CADA", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp)
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        INTERVAL_OPTIONS.forEach { opt ->
                            ReminderIntervalPill(
                                label = opt.label,
                                selected = selected == opt,
                                onClick = { selected = opt },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── Notas
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )

                HorizontalDivider()

                // Botones
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50)
                    ) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val client = selectedClient ?: return@Button
                            val nextCal = Calendar.getInstance().apply {
                                timeInMillis = calendar.timeInMillis
                                add(Calendar.MONTH, selected.value)
                            }
                            onSave(MaintenanceReminder(
                                equipmentType  = equipmentType,
                                clientId       = client.id,
                                intervalValue  = selected.value,
                                intervalUnit   = selected.unit,
                                intervalMonths = selected.value,
                                lastServiceDate = 0L,
                                nextServiceDate = calendar.timeInMillis,
                                notes          = notes.trim(),
                                source         = REMINDER_SOURCE_MANUAL
                            ))
                        },
                        enabled = selectedClient != null && dateStr.isNotBlank() && timeStr.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    Brush.linearGradient(listOf(Primary40, Secondary40)),
                                    RoundedCornerShape(50)
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Guardar", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

// ─── Detalle del mantenimiento ─────────────────────────────────────────────────
@Composable
fun ReminderDetailDialog(
    r: MaintenanceReminder,
    client: Client?,
    onDismiss: () -> Unit,
    onMarkDone: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val now = System.currentTimeMillis()
    val isDue = r.nextServiceDate <= now
    val isAuto = r.source == REMINDER_SOURCE_AUTO
    val shortId = "MT-${r.id.take(6).uppercase()}"
    val color = if (isDue) StatusLowStock else StatusCompleted

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {

                // ID + tipo en la parte superior
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Primary40.copy(0.12f)) {
                        Text(shortId,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = Primary40)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAuto) Secondary40.copy(0.15f) else Tertiary40.copy(0.15f)
                    ) {
                        Text(if (isAuto) "Auto" else "Manual",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isAuto) Secondary40 else Tertiary40)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                // Cliente
                if (client != null) {
                    DetailRow(
                        icon = { Icon(Icons.Default.Person, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                        label = "Cliente",
                        value = "${client.name}${if (client.lastName.isNotBlank()) " ${client.lastName}" else ""}"
                    )
                }

                // Equipo
                if (r.equipmentType.isNotBlank()) {
                    DetailRow(
                        icon = { Icon(Icons.Default.Build, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                        label = "Equipo",
                        value = r.equipmentType
                    )
                }

                // Notas
                if (r.notes.isNotBlank()) {
                    DetailRow(
                        icon = { Icon(Icons.Default.Notes, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                        label = "Notas",
                        value = r.notes
                    )
                }

                // Fecha(s) según fuente
                if (isAuto) {
                    if (r.lastServiceDate > 0L) {
                        DetailRow(
                            icon = { Icon(Icons.Default.History, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                            label = "Último servicio",
                            value = sdf.format(Date(r.lastServiceDate))
                        )
                    }
                    DetailRow(
                        icon = { Icon(Icons.Default.CalendarToday, null, tint = color, modifier = Modifier.size(20.dp)) },
                        label = "Próximo servicio",
                        value = sdf.format(Date(r.nextServiceDate)),
                        valueColor = color
                    )
                    val intervalText = when (r.intervalValue) {
                        12 -> "Cada 1 año"
                        else -> "Cada ${r.intervalValue} mes(es)"
                    }
                    DetailRow(
                        icon = { Icon(Icons.Default.Repeat, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                        label = "Intervalo",
                        value = intervalText
                    )
                } else {
                    DetailRow(
                        icon = { Icon(Icons.Default.CalendarToday, null, tint = color, modifier = Modifier.size(20.dp)) },
                        label = "Fecha programada",
                        value = if (r.nextServiceDate > 0L) sdfFull.format(Date(r.nextServiceDate)) else "Sin fecha",
                        valueColor = color
                    )
                    val intervalText = when (r.intervalValue) {
                        12 -> "Repite cada 1 año"
                        else -> "Repite cada ${r.intervalValue} mes(es)"
                    }
                    DetailRow(
                        icon = { Icon(Icons.Default.Repeat, null, tint = Primary40, modifier = Modifier.size(20.dp)) },
                        label = "Intervalo",
                        value = intervalText
                    )
                }

                HorizontalDivider()

                // Botones de acción
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isDue) {
                        Button(
                            onClick = onMarkDone,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusCompleted)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Marcar como realizado")
                        }
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusLowStock),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusLowStock)
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Eliminar recordatorio")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        icon()
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (valueColor == Color.Unspecified)
                    MaterialTheme.colorScheme.onSurface else valueColor)
        }
    }
}

@Composable
private fun ReminderListDialog(
    title: String,
    reminders: List<MaintenanceReminder>,
    clients: List<Client>,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val sdfFull = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val now = System.currentTimeMillis()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.93f).fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF5F5F5),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Engineering, null, tint = Primary40,
                            modifier = Modifier.size(22.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Surface(shape = RoundedCornerShape(50),
                            color = Primary40.copy(alpha = 0.12f)) {
                            Text(
                                reminders.size.toString(),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold, color = Primary40
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider()

                if (reminders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin mantenimientos",
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(reminders, key = { it.id }) { r ->
                            val client = clients.find { it.id == r.clientId }
                            val clientName = client?.let {
                                "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}"
                            } ?: "Sin cliente"
                            val isDue = r.nextServiceDate <= now
                            val daysLeft = ((r.nextServiceDate - now) / 86_400_000L).toInt()
                            val isAuto = r.source == REMINDER_SOURCE_AUTO
                            val accentColor = when {
                                isDue || daysLeft <= 3 -> Color(0xFFD32F2F)
                                daysLeft in 4..7 -> Color(0xFFFF8F00)
                                daysLeft in 8..30 -> Primary40
                                else -> Color(0xFF00897B)
                            }
                            val equipIcon = when (r.equipmentType.lowercase()) {
                                "nevera" -> Icons.Default.AcUnit
                                "aire acondicionado" -> Icons.Default.Air
                                "lavadora" -> Icons.Default.WaterDrop
                                else -> Icons.Default.Build
                            }
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                tonalElevation = 0.dp,
                                shadowElevation = 2.dp
                            ) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                                        .background(accentColor,
                                            RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)))
                                    Row(
                                        modifier = Modifier.padding(12.dp).weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(36.dp).clip(CircleShape)
                                                .background(accentColor.copy(alpha = 0.12f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(equipIcon, null, tint = accentColor,
                                                modifier = Modifier.size(18.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(clientName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface)
                                            Text(r.equipmentType.ifBlank { "Equipo" },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            if (isAuto && r.lastServiceDate > 0L) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("ÚLTIMO SERVICIO",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            letterSpacing = 0.3.sp)
                                                        Text(sdf.format(Date(r.lastServiceDate)),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Medium)
                                                    }
                                                    Column(horizontalAlignment = Alignment.End) {
                                                        Text("PRÓXIMO SERVICIO",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            letterSpacing = 0.3.sp)
                                                        Text(sdf.format(Date(r.nextServiceDate)),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = accentColor)
                                                    }
                                                }
                                            } else {
                                                Row(modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        if (isAuto) "PRÓXIMO SERVICIO" else "FECHA DEL SERVICIO",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                    Text(
                                                        if (r.nextServiceDate > 0L) {
                                                            if (isAuto) sdf.format(Date(r.nextServiceDate))
                                                            else sdfFull.format(Date(r.nextServiceDate))
                                                        } else "Sin fecha",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = accentColor)
                                                }
                                            }
                                        }
                                        Surface(shape = RoundedCornerShape(8.dp),
                                            color = accentColor.copy(alpha = 0.12f)) {
                                            Text(
                                                if (isDue) "VENCIDO" else "EN $daysLeft D.",
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = accentColor)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderIntervalPill(
    label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) Color.Transparent else Color(0xFFF0F0F0),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (selected) Modifier.background(
                        Brush.linearGradient(listOf(Primary40, Secondary40)), RoundedCornerShape(50)
                    ) else Modifier
                )
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) Color.White else Color(0xFF6B6B6B))
        }
    }
}
