package com.gestion.itinerario.ui.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.*
import com.gestion.itinerario.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// ─── Constantes de color ──────────────────────────────────────────────────────
private val ColorCitaChart  = Color(0xFF1565C0)
private val ColorMantChart  = Color(0xFF2E7D32)

/** Degradado de un solo tono (más claro → más oscuro) que sigue la paleta activa, evitando choques de color. */
private fun shadeGradient(base: Color): List<Color> = listOf(
    androidx.compose.ui.graphics.lerp(base, Color.White, 0.16f),
    androidx.compose.ui.graphics.lerp(base, Color.Black, 0.18f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
    profileViewModel: com.gestion.itinerario.ui.profile.ProfileViewModel = hiltViewModel()
) {
    val inRepair         by viewModel.inRepairCount.collectAsStateWithLifecycle()
    val todayCitas       by viewModel.todayAppointments.collectAsStateWithLifecycle()
    val pendingCitas     by viewModel.pendingAppointments.collectAsStateWithLifecycle()
    val totalServices    by viewModel.totalServices.collectAsStateWithLifecycle()
    val chartData        by viewModel.chartData.collectAsStateWithLifecycle()
    val clients          by viewModel.clients.collectAsStateWithLifecycle()
    val inRepairList     by viewModel.inRepairList.collectAsStateWithLifecycle()
    val todayList        by viewModel.todayList.collectAsStateWithLifecycle()
    val pendingList      by viewModel.pendingList.collectAsStateWithLifecycle()
    val completedList    by viewModel.completedList.collectAsStateWithLifecycle()
    val nextAppt         by viewModel.nextAppointment.collectAsStateWithLifecycle()
    val todayStats       by viewModel.todayStats.collectAsStateWithLifecycle()
    val userName         by viewModel.userName.collectAsStateWithLifecycle()
    val todayCompleted   by viewModel.todayCompletedCount.collectAsStateWithLifecycle()
    val todayCompletedL  by viewModel.todayCompletedList.collectAsStateWithLifecycle()
    val notifications    by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val recentActivity   by viewModel.recentActivity.collectAsStateWithLifecycle()
    val categoryStats    by viewModel.categoryStats.collectAsStateWithLifecycle()
    val companyProfile   by profileViewModel.profile.collectAsStateWithLifecycle()

    var showNewCitaDialog   by remember { mutableStateOf(false) }
    var quickCategoryFilter by remember { mutableStateOf("") }
    var showUserMenu        by remember { mutableStateOf(false) }
    var showNotifications    by remember { mutableStateOf(false) }
    var clearedNotifications by remember { mutableStateOf(false) }
    var notificationsReadAt  by remember { mutableStateOf(0L) }
    var showMoreDetail      by remember { mutableStateOf(false) }
    var showDeleteConfirm   by remember { mutableStateOf(false) }
    var activeSheet         by remember { mutableStateOf<String?>(null) }
    var detailAppointment   by remember { mutableStateOf<Appointment?>(null) }
    var selectedCategory    by remember { mutableStateOf<ServiceCategory?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val clientMap = remember(clients) { clients.associateBy { it.id } }
    fun clientFullName(id: String) = clientMap[id]?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"
    val professionalName = companyProfile.ownerName.ifBlank { companyProfile.companyName }.ifBlank { "Profesional asignado" }

    // Refresco cada 30 segundos para la cuenta regresiva
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) { delay(30_000L); currentTime = System.currentTimeMillis() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(
                start  = 16.dp,
                end    = 16.dp,
                top    = innerPadding.calculateTopPadding() + 16.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Spacer(Modifier.height(10.dp))
                val greetName = userName.ifBlank { "" }
                Text(
                    if (greetName.isNotBlank()) "¡Hola, $greetName! 👋" else "¡Hola! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("¿En qué podemos ayudarte hoy?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val displayCompanyName = companyProfile.companyName.ifBlank { "ServiceFlow" }
                Spacer(Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Primary40.copy(alpha = 0.12f)
                ) {
                    Text(displayCompanyName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary40)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.padding(top = 4.dp, end = 2.dp)) {
                    IconButton(onClick = { showNotifications = true; notificationsReadAt = System.currentTimeMillis() }, modifier = Modifier.size(44.dp)) {
                        val activeCount = notifications.count { !it.isCompleted }
                    BadgedBox(badge = {
                            if (!clearedNotifications && activeCount > 0) {
                                Badge(modifier = Modifier.offset(x = (-3).dp, y = 3.dp)) {
                                    Text(activeCount.coerceAtMost(9).toString())
                                }
                            }
                        }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones",
                                tint = Primary80, modifier = Modifier.size(26.dp))
                        }
                    }
                }
                Box {
                    IconButton(onClick = { showUserMenu = true }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Menú usuario",
                            tint = Primary80, modifier = Modifier.size(32.dp))
                    }
                    DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Perfil de empresa") },
                            leadingIcon = { Icon(Icons.Default.Business, null, tint = Primary80) },
                            onClick = { showUserMenu = false; onNavigate(com.gestion.itinerario.ui.Routes.PROFILE) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            leadingIcon = { Icon(Icons.Default.Logout, null) },
                            onClick = { showUserMenu = false; onLogout() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showUserMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }

        // ── Próxima Cita (tarjeta destacada con degradado, siempre arriba) ────
        NextAppointmentHeroCard(
            appointment = nextAppt,
            currentTime = currentTime,
            clientName = nextAppt?.let { clientFullName(it.clientId) } ?: "",
            professionalName = professionalName,
            onViewDetails = { detailAppointment = nextAppt }
        )

        // ── Total de servicios de hoy (grande/destacado) ──────────────────────
        CompletedServicesCard(total = todayCompleted, onClick = { activeSheet = "completedToday" })

        // ── Botón "Más detalle": expande En Reparación / Citas Hoy / Pendientes ──
        Surface(
            onClick = { showMoreDetail = !showMoreDetail },
            modifier = Modifier.align(Alignment.End),
            shape = RoundedCornerShape(50),
            color = Primary40.copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(if (showMoreDetail) "Menos detalle" else "Más detalle",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary40)
                Icon(if (showMoreDetail) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null, tint = Primary40, modifier = Modifier.size(20.dp))
            }
        }

        if (showMoreDetail) {
            // ── Fila: En Reparación (morado) | Citas Hoy (azul) ───────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "En Reparación",
                    value = inRepair.toString(),
                    subtitle = "Servicios en proceso",
                    icon = Icons.Default.Build,
                    gradientColors = shadeGradient(MaterialTheme.colorScheme.primary),
                    valueColor = Color.White,
                    onClick = { activeSheet = "repair" }
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Citas Hoy",
                    value = todayCitas.toString(),
                    subtitle = "Citas programadas hoy",
                    icon = Icons.Default.CalendarToday,
                    gradientColors = shadeGradient(MaterialTheme.colorScheme.secondary),
                    valueColor = Color.White,
                    onClick = { activeSheet = "today" }
                )
            }

            // ── Citas Pendientes (amarillo) ───────────────────────────────────
            PendingCard(
                modifier = Modifier.fillMaxWidth(),
                count = pendingCitas,
                onClick = { activeSheet = "pending" }
            )
        }

        // ── Acciones Rápidas ──────────────────────────────────────────────────
        Text("Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground)

        QuickActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "Nueva Cita / Mantenimiento / Instalación",
            icon = Icons.Default.EventAvailable,
            color = MaterialTheme.colorScheme.secondary
        ) { quickCategoryFilter = ""; showNewCitaDialog = true }

        // ── Servicios Destacados (cuadrícula 2x2: ver estado actual de la categoría) ──
        Text("Servicios Destacados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeaturedServiceCard(modifier = Modifier.weight(1f), category = ServiceCategory.LAVADORA,
                    onClick = { selectedCategory = ServiceCategory.LAVADORA })
                FeaturedServiceCard(modifier = Modifier.weight(1f), category = ServiceCategory.REFRIGERACION,
                    onClick = { selectedCategory = ServiceCategory.REFRIGERACION })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FeaturedServiceCard(modifier = Modifier.weight(1f), category = ServiceCategory.AIRE_ACONDICIONADO,
                    onClick = { selectedCategory = ServiceCategory.AIRE_ACONDICIONADO })
                FeaturedServiceCard(modifier = Modifier.weight(1f), category = ServiceCategory.OTROS,
                    onClick = { selectedCategory = ServiceCategory.OTROS })
            }
        }

        // ── Actividad Reciente (últimas 24 horas) ─────────────────────────────
        RecentActivityCard(activity = recentActivity)

        // ── Gráficos del Día + Actividad de los últimos 7 días (fusionados) ────
        DailyChartsCard(stats = todayStats, chartData = chartData)
    }

    // ── Bottom Sheet de servicios ─────────────────────────────────────────────
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState
        ) {
            ServicesBottomSheet(
                type = activeSheet!!,
                inRepairList = inRepairList,
                todayList = todayList,
                pendingList = pendingList,
                completedList = completedList,
                completedTodayList = todayCompletedL,
                clients = clients
            )
        }
    }

    // ── Diálogo Eliminar Cuenta ───────────────────────────────────────────────
    if (showDeleteConfirm) {
        val authVm: com.gestion.itinerario.ui.auth.AuthViewModel = hiltViewModel()
        val authState by authVm.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(authState) {
            if (authState is com.gestion.itinerario.ui.auth.AuthUiState.Success) {
                authVm.resetState()
                onLogout()
            }
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás segura de que querés eliminar tu cuenta? Esta acción no se puede deshacer y perderás todos tus datos.") },
            confirmButton = {
                Button(
                    onClick = { authVm.deleteAccount() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = authState !is com.gestion.itinerario.ui.auth.AuthUiState.Loading
                ) {
                    if (authState is com.gestion.itinerario.ui.auth.AuthUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                    } else { Text("Eliminar") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    // ── Diálogo Nueva Cita ────────────────────────────────────────────────────
    if (showNewCitaDialog) {
        NewAppointmentDialog(
            clients = clients,
            initialEquipmentType = quickCategoryFilter,
            onDismiss = { showNewCitaDialog = false; quickCategoryFilter = "" },
            onSave = { appointment ->
                viewModel.saveAppointment(appointment)
                showNewCitaDialog = false
                quickCategoryFilter = ""
            }
        )
    }

    // ── Panel de notificaciones (pantalla completa) ───────────────────────────
    if (showNotifications) {
        NotificationsScreen(
            notifications = notifications,
            cleared = clearedNotifications,
            readBefore = notificationsReadAt,
            grouped = { items -> viewModel.groupByRecency(items) { it.timestamp } },
            onClearAll = { clearedNotifications = true },
            onDismiss = { showNotifications = false }
        )
    }

    // ── Mini-dashboard de categoría de servicio (desde "Servicios Destacados") ──
    selectedCategory?.let { cat ->
        CategoryDashboardScreen(
            stats = categoryStats[cat] ?: CategoryStats(category = cat),
            onDismiss = { selectedCategory = null }
        )
    }

    // ── Pantalla de Detalle del Servicio ──────────────────────────────────────
    detailAppointment?.let { appt ->
        com.gestion.itinerario.ui.services.ServiceDetailScreen(
            appointment = appt,
            clientName = clientFullName(appt.clientId),
            professionalName = professionalName,
            clientPhone = clientMap[appt.clientId]?.phone ?: "",
            onDismiss = { detailAppointment = null }
        )
    }
}

// ─── Tarjeta Citas Pendientes (amarillo) ──────────────────────────────────────
@Composable
fun PendingCard(modifier: Modifier, count: Int, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(shadeGradient(MaterialTheme.colorScheme.tertiary)))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Icon(Icons.Default.CalendarMonth, null,
                tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = Color.White)
            Text("Citas Pendientes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
            Text("programadas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f))
        }
    }
}

// ─── Tarjeta destacada de Próxima Cita (degradado, ícono por categoría, "Ver detalles") ──
@Composable
fun NextAppointmentHeroCard(
    appointment: Appointment?,
    currentTime: Long,
    clientName: String,
    professionalName: String,
    onViewDetails: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PRÓXIMA CITA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.75f))

            if (appointment == null) {
                Text("No tienes citas programadas próximamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f))
            } else {
                val category = classifyServiceCategory(appointment.equipmentType, appointment.notes)
                val sdf = remember { SimpleDateFormat("EEE dd/MM · hh:mm a", Locale.getDefault()) }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(category.icon, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text(buildString {
                            append(appointment.serviceType.shortLabel())
                            if (appointment.equipmentType.isNotBlank()) append(" · ${appointment.equipmentType}")
                        }, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text(sdf.format(Date(appointment.dateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.25f))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(36.dp))
                    Column {
                        Text(clientName.ifBlank { "Cliente" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Light, color = Color.White)
                        Text("Atiende: $professionalName",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f))
                    }
                }

                Button(
                    onClick = onViewDetails,
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Ver detalles", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── Tarjeta de "Servicios Destacados" (Electricidad / Refrigeración / A.A.) ──
@Composable
fun FeaturedServiceCard(modifier: Modifier, category: ServiceCategory, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(50))
                    .background(category.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(26.dp))
            }
            Text(category.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

// ─── Tarjeta de "Actividad Reciente" (últimas 24 horas) ───────────────────────
@Composable
fun RecentActivityCard(activity: List<ActivityItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Actividad Reciente",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            if (activity.isEmpty()) {
                Text("Sin actividad en las últimas 24 horas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                activity.take(8).forEachIndexed { idx, item ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.color.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text(item.subtitle, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(sdf.format(Date(item.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (item.amount != null) {
                                Text("$${"%.2f".format(item.amount)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusCompleted)
                            }
                        }
                    }
                    if (idx < activity.take(8).lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

// ─── Pantalla completa de Notificaciones ──────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    cleared: Boolean,
    readBefore: Long,
    grouped: (List<NotificationItem>) -> List<Pair<String, List<NotificationItem>>>,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    val visible = if (cleared) emptyList() else notifications

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Notificaciones", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp)
                ) {
                    if (visible.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp),
                            contentAlignment = Alignment.TopCenter) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Notifications, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                                Text(
                                    if (cleared) "Has marcado todas tus notificaciones como leídas."
                                    else "Sin notificaciones recientes.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            grouped(visible).forEach { (label, group) ->
                                Text(label.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    group.forEach { item -> NotificationRow(item) }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            onClick = onClearAll
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Limpiar todas las notificaciones",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Fila de notificación con barra de acento de color ────────────────────────
@Composable
fun NotificationRow(item: NotificationItem) {
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dim = item.isCompleted
    val accentColor = if (dim) Color.Gray.copy(alpha = 0.35f) else item.color
    val cardColor   = if (dim) Color(0xFFF5F5F5) else Color.White
    val textColor   = if (dim) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                      else MaterialTheme.colorScheme.onSurface
    val subColor    = if (dim) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                      else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardColor,
        shadowElevation = if (dim) 0.dp else 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor))
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(item.icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.title, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (dim) FontWeight.Normal else FontWeight.SemiBold,
                        color = textColor)
                    Text(item.subtitle, style = MaterialTheme.typography.labelSmall, color = subColor)
                    if (item.statusLabel.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(accentColor.copy(alpha = if (dim) 0.08f else 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(item.statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (dim) subColor else accentColor)
                        }
                    }
                }
                Text(sdf.format(Date(item.timestamp)),
                    style = MaterialTheme.typography.labelSmall, color = subColor)
            }
        }
    }
}

// ─── Tarjeta Total de Servicios (verde) ───────────────────────────────────────
@Composable
fun CompletedServicesCard(total: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Total de Servicios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f))
                Text(total.toString(), fontSize = 42.sp, fontWeight = FontWeight.Bold,
                    color = Color.White)
                Text("completados hoy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.BarChart, null,
                tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
        }
    }
}

// ─── Gráficos del Día (Progreso/Estados) + Actividad de los últimos 7 días ────
@Composable
fun DailyChartsCard(stats: TodayStats, chartData: List<ChartDay>) {
    var selectedTab by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gráficos del Día",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Progreso" to 0, "Estados" to 1).forEach { (label, idx) ->
                    val sel = selectedTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (sel) Brush.linearGradient(listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                ))
                                else Brush.linearGradient(listOf(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    MaterialTheme.colorScheme.surfaceVariant
                                ))
                            )
                            .clickable { selectedTab = idx }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (sel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (selectedTab == 0) DailyProgressBars(stats) else DailyPieChart(stats)

            if (chartData.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(16.dp))
                Text("Actividad — Últimos 7 días",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(color = MaterialTheme.colorScheme.primary,   label = "Cita / Reparación")
                    LegendDot(color = MaterialTheme.colorScheme.secondary, label = "Mantenimiento")
                }
                Spacer(Modifier.height(12.dp))
                WeeklyActivityChart(chartData = chartData)
            }
        }
    }
}

@Composable
private fun DailyProgressBars(stats: TodayStats) {
    if (stats.citasTotal == 0 && stats.mantTotal == 0) {
        Text("Sin actividad programada para hoy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        if (stats.citasTotal > 0) {
            val progress = stats.citasCompleted.toFloat() / stats.citasTotal
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Citas / Reparaciones",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("${stats.citasCompleted}/${stats.citasTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = trackColor
                )
                Text("${stats.citasCompleted} de ${stats.citasTotal} completadas hoy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (stats.mantTotal > 0) {
            val progress = stats.mantCompleted.toFloat() / stats.mantTotal
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mantenimientos",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("${stats.mantCompleted}/${stats.mantTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = trackColor
                )
                Text("${stats.mantCompleted} de ${stats.mantTotal} completados hoy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DailyPieChart(stats: TodayStats) {
    val hasData = stats.totalCompleted > 0 || stats.totalPending > 0 || stats.totalCancelled > 0
    if (!hasData) {
        Text("Sin actividad programada para hoy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val total = (stats.totalCompleted + stats.totalPending + stats.totalCancelled).toFloat()
    // Capturar colores fuera del Canvas (DrawScope no es @Composable)
    val colorCompleted = MaterialTheme.colorScheme.primary
    val colorPending   = MaterialTheme.colorScheme.secondary
    val colorCancelled = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val diameter = size.minDimension * 0.85f
            val tl = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val sz = Size(diameter, diameter)
            var startAngle = -90f
            if (stats.totalCompleted > 0) {
                val sweep = (stats.totalCompleted / total) * 360f
                drawArc(color = colorCompleted, startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
                startAngle += sweep
            }
            if (stats.totalPending > 0) {
                val sweep = (stats.totalPending / total) * 360f
                drawArc(color = colorPending, startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
                startAngle += sweep
            }
            if (stats.totalCancelled > 0) {
                val sweep = (stats.totalCancelled / total) * 360f
                drawArc(color = colorCancelled, startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
            }
        }

        Spacer(Modifier.width(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendDot(color = MaterialTheme.colorScheme.primary,   label = "Listas: ${stats.totalCompleted}")
            LegendDot(color = MaterialTheme.colorScheme.secondary, label = "Pendientes: ${stats.totalPending}")
            LegendDot(color = MaterialTheme.colorScheme.error,     label = "Canceladas: ${stats.totalCancelled}")
        }
    }
}

// ─── Gráfico de barras semanal (esquinas superiores redondeadas, por color) ───
@Composable
private fun WeeklyActivityChart(chartData: List<ChartDay>) {
    val maxVal = chartData.maxOf { it.citas + it.mantenimientos }.coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    // Colores siguen la paleta del usuario
    val colorCitas = MaterialTheme.colorScheme.primary
    val colorMant  = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(145.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight()
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(maxVal, maxVal / 2, 0).forEach { v ->
                Text(v.toString(), fontSize = 9.sp,
                    color = labelColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth())
            }
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)) {
                val barGroupW = size.width / chartData.size
                val barW = barGroupW * 0.40f   // barras más gruesas
                val gap  = barW * 0.15f
                val maxH = size.height
                val radius = (barW / 2.5f).coerceAtMost(12.dp.toPx())
                val gridAlpha = 0.18f
                drawLine(Color.Gray.copy(gridAlpha), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                drawLine(Color.Gray.copy(gridAlpha), Offset(0f, maxH / 2), Offset(size.width, maxH / 2), 1.dp.toPx())
                drawLine(Color.Gray.copy(gridAlpha), Offset(0f, maxH), Offset(size.width, maxH), 1.dp.toPx())

                fun roundedTopBar(x: Float, top: Float, width: Float, bottom: Float): Path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            left = x, top = top, right = x + width, bottom = bottom,
                            topLeftCornerRadius = CornerRadius(radius, radius),
                            topRightCornerRadius = CornerRadius(radius, radius),
                            bottomLeftCornerRadius = CornerRadius.Zero,
                            bottomRightCornerRadius = CornerRadius.Zero
                        )
                    )
                }

                chartData.forEachIndexed { i, day ->
                    val groupX = i * barGroupW + (barGroupW - barW * 2 - gap) / 2f
                    val citasH = (day.citas.toFloat() / maxVal) * maxH
                    if (citasH > 0f) {
                        drawPath(roundedTopBar(groupX, maxH - citasH, barW, maxH), color = colorCitas)
                    }
                    val mantH = (day.mantenimientos.toFloat() / maxVal) * maxH
                    if (mantH > 0f) {
                        drawPath(roundedTopBar(groupX + barW + gap, maxH - mantH, barW, maxH), color = colorMant)
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                chartData.forEach { day ->
                    Text(day.label, modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor)
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(color))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Bottom Sheet de servicios ────────────────────────────────────────────────
@Composable
fun ServicesBottomSheet(
    type: String,
    inRepairList: Pair<List<Appointment>, List<ServiceOrder>>,
    todayList: List<Appointment>,
    pendingList: List<Appointment>,
    completedList: Pair<List<Appointment>, List<ServiceOrder>>,
    completedTodayList: Pair<List<Appointment>, List<ServiceOrder>> = Pair(emptyList(), emptyList()),
    clients: List<Client>
) {
    val (title, appointments, orders) = when (type) {
        "repair"         -> Triple("En Reparación",            inRepairList.first,        inRepairList.second)
        "today"          -> Triple("Citas Hoy",                todayList,                 emptyList<ServiceOrder>())
        "pending"        -> Triple("Citas Pendientes",         pendingList,               emptyList<ServiceOrder>())
        "completed"      -> Triple("Servicios Completados",    completedList.first,       completedList.second)
        "completedToday" -> Triple("Servicios de Hoy",         completedTodayList.first,  completedTodayList.second)
        else             -> Triple("Servicios",                emptyList<Appointment>(),  emptyList<ServiceOrder>())
    }

    val clientMap = remember(clients) { clients.associateBy { it.id } }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("${appointments.size + orders.size} registro(s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        if (appointments.isEmpty() && orders.isEmpty()) {
            Text("No hay registros en esta categoría.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        appointments.forEachIndexed { idx, appt ->
            AppointmentSheetItem(appt, clientMap)
            if (idx < appointments.lastIndex || orders.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
        orders.forEachIndexed { idx, order ->
            ServiceOrderSheetItem(order, clientMap)
            if (idx < orders.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun AppointmentSheetItem(appt: Appointment, clientMap: Map<String, Client>) {
    val clientName = clientMap[appt.clientId]
        ?.let { "${it.name} ${it.lastName}".trim() }
        ?: appt.notes.split(" — ").firstOrNull()?.take(30)
        ?: "Cliente"

    val dateStr = remember(appt.dateTime) {
        SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.getDefault()).format(Date(appt.dateTime))
    }
    val serviceLabel = when (appt.serviceType) {
        ServiceType.MAINTENANCE  -> "Mantenimiento"
        ServiceType.REPAIR       -> "Reparación"
        ServiceType.INSTALLATION -> "Instalación"
    }
    val (statusLabel, statusColor) = when (appt.status) {
        AppointmentStatus.SCHEDULED   -> "Programada"  to Color(0xFF1565C0)
        AppointmentStatus.IN_PROGRESS -> "En proceso"  to Color(0xFFE65100)
        AppointmentStatus.COMPLETED   -> "Completada"  to Color(0xFF2E7D32)
        AppointmentStatus.CANCELLED   -> "Cancelada"   to Color(0xFFB71C1C)
    }

    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(
            when (appt.serviceType) {
                ServiceType.REPAIR       -> Icons.Default.Build
                ServiceType.INSTALLATION -> Icons.Default.HomeRepairService
                ServiceType.MAINTENANCE  -> Icons.Default.Settings
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(clientName, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium)
            Text(buildString {
                append(serviceLabel)
                if (appt.equipmentType.isNotBlank()) append(" · ${appt.equipmentType}")
            }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(dateStr, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
            Text(statusLabel,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall, color = statusColor,
                fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ServiceOrderSheetItem(order: ServiceOrder, clientMap: Map<String, Client>) {
    val clientName = clientMap[order.clientId]
        ?.let { "${it.name} ${it.lastName}".trim() } ?: "Cliente"

    val (statusLabel, statusColor) = when (order.status) {
        ServiceStatus.PENDING     -> "Pendiente"  to Color(0xFF757575)
        ServiceStatus.IN_PROGRESS -> "En proceso" to Color(0xFFE65100)
        ServiceStatus.COMPLETED   -> "Completado" to Color(0xFF2E7D32)
    }

    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.Build, null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp).padding(top = 2.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(clientName, fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium)
            if (order.description.isNotBlank()) {
                Text(order.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (order.equipmentType.isNotBlank()) {
                Text(order.equipmentType, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.15f)) {
            Text(statusLabel,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall, color = statusColor,
                fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Diálogo Nueva Cita ───────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAppointmentDialog(
    clients: List<Client>,
    initialEquipmentType: String = "",
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var selectedClient         by remember { mutableStateOf<Client?>(null) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var notes                  by remember { mutableStateOf("") }
    var serviceType            by remember { mutableStateOf(ServiceType.MAINTENANCE) }
    var equipmentType          by remember { mutableStateOf(initialEquipmentType) }
    val context  = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dateStr  by remember { mutableStateOf("") }
    var timeStr  by remember { mutableStateOf("") }

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
    val equipmentTypes = listOf("Nevera", "Aire Acondicionado", "Lavadora", "Otro")
    val equipmentIcons = mapOf(
        "Nevera"            to Icons.Default.AcUnit,
        "Aire Acondicionado" to Icons.Default.Air,
        "Lavadora"          to Icons.Default.WaterDrop,
        "Otro"              to Icons.Default.MoreHoriz
    )

    val timePicker = TimePickerDialog(context, { _, h, min ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
        timeStr = String.format("%02d:%02d", h, min)
        dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d)
        dateStr = String.format("%02d/%02d/%04d", d, m + 1, y)
        timePicker.show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            Scaffold(
                containerColor = Color.White,
                topBar = {
                    TopAppBar(
                        title = { Text("Nueva Cita", fontWeight = FontWeight.Bold) },
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
                                    val clientName = "${client.name}${if (client.lastName.isNotBlank()) " ${client.lastName}" else ""}"
                                    onSave(Appointment(
                                        clientId      = client.id,
                                        dateTime      = calendar.timeInMillis,
                                        serviceType   = serviceType,
                                        equipmentType = equipmentType,
                                        notes         = if (notes.isNotBlank()) "$clientName — $notes" else clientName
                                    ))
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
                    // ── Banner ────────────────────────────────────────────────
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
                                    Text("Agendar Servicio", style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold)
                                    Text("Complete los detalles para la visita técnica.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    // ── Cliente ───────────────────────────────────────────────
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

                    // ── Fecha y Hora ──────────────────────────────────────────
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
                                    timeStr.ifBlank { "--:-- --" },
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

                    // ── Motivo ────────────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("MOTIVO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()) {
                            serviceTypes.forEach { st ->
                                val selected = serviceType == st
                                Surface(
                                    onClick = { serviceType = st },
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                            else Color.White,
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
                                            tint = if (selected) Color.White
                                                   else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp))
                                        Text(serviceLabels[st]!!,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (selected) Color.White
                                                    else MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    // ── Tipo de Equipo ────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TIPO DE EQUIPO", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            equipmentTypes.chunked(2).forEach { row ->
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
                                                Text(et, style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = if (selected) FontWeight.SemiBold
                                                                 else FontWeight.Normal,
                                                    color = if (selected)
                                                        MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Notas adicionales ─────────────────────────────────────
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

// ─── Componentes comunes ──────────────────────────────────────────────────────

@Composable
fun StatCard(
    modifier: Modifier,
    title: String,
    value: String,
    subtitle: String = "",
    icon: ImageVector,
    gradientColors: List<Color>,
    valueColor: Color,
    onClick: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(600), label = "alpha")

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradientColors))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Column {
            Icon(icon, contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(10.dp))
            Text(value, fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = valueColor.copy(alpha = alpha))
            Text(title, style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
            if (subtitle.isNotBlank()) {
                Text(subtitle, style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}

/** Botón tipo "píldora" con fondo en gradiente, similar a los accesos rápidos del diseño de referencia. */
@Composable
fun QuickActionButton(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    val gradient = Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, color))
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.background(gradient).padding(vertical = 16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = Color.White,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
