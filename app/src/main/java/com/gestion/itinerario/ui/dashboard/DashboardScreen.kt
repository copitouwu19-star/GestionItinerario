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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

private val PurpleGradient  = listOf(Color(0xFF4A0072), Color(0xFF9C27B0))   // En Reparación
private val BlueGradient    = listOf(Color(0xFF0D47A1), Color(0xFF1565C0))   // Citas Hoy
private val YellowGradient  = listOf(Color(0xFF5D3200), Color(0xFFFF8F00))   // Citas Pendientes
private val GreenGradient   = listOf(Color(0xFF1B5E20), Color(0xFF43A047))   // Total Servicios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    innerPadding: PaddingValues,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val inRepair      by viewModel.inRepairCount.collectAsStateWithLifecycle()
    val todayCitas    by viewModel.todayAppointments.collectAsStateWithLifecycle()
    val pendingCitas  by viewModel.pendingAppointments.collectAsStateWithLifecycle()
    val totalServices by viewModel.totalServices.collectAsStateWithLifecycle()
    val chartData     by viewModel.chartData.collectAsStateWithLifecycle()
    val clients       by viewModel.clients.collectAsStateWithLifecycle()
    val inRepairList  by viewModel.inRepairList.collectAsStateWithLifecycle()
    val todayList     by viewModel.todayList.collectAsStateWithLifecycle()
    val pendingList   by viewModel.pendingList.collectAsStateWithLifecycle()
    val completedList by viewModel.completedList.collectAsStateWithLifecycle()
    val nextAppt      by viewModel.nextAppointment.collectAsStateWithLifecycle()
    val todayStats    by viewModel.todayStats.collectAsStateWithLifecycle()
    val userName      by viewModel.userName.collectAsStateWithLifecycle()

    var showNewCitaDialog by remember { mutableStateOf(false) }
    var showUserMenu      by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var activeSheet       by remember { mutableStateOf<String?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            Column {
                if (userName.isNotBlank()) {
                    Text("¡Bienvenido, $userName!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                } else {
                    Text("Panel de Control",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground)
                }
                Text("Visión general del negocio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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

        // ── Fila 1: En Reparación (morado) | Citas Hoy (azul) ────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "En Reparación",
                value = inRepair.toString(),
                subtitle = "Servicios en proceso",
                icon = Icons.Default.Build,
                gradientColors = PurpleGradient,
                valueColor = Color(0xFFCE93D8),
                onClick = { activeSheet = "repair" }
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Citas Hoy",
                value = todayCitas.toString(),
                subtitle = "Citas programadas hoy",
                icon = Icons.Default.CalendarToday,
                gradientColors = BlueGradient,
                valueColor = Color(0xFF90CAF9),
                onClick = { activeSheet = "today" }
            )
        }

        // ── Fila 2: Citas Pendientes (amarillo) | Próxima Cita ────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PendingCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                count = pendingCitas,
                onClick = { activeSheet = "pending" }
            )
            NextAppointmentCard(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                appointment = nextAppt,
                currentTime = currentTime
            )
        }

        // ── Total de Servicios (verde) ────────────────────────────────────────
        CompletedServicesCard(total = totalServices, onClick = { activeSheet = "completed" })

        // ── Acciones Rápidas ──────────────────────────────────────────────────
        Text("Acciones Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground)

        QuickActionButton(
            modifier = Modifier.fillMaxWidth(),
            label = "Nueva Cita / Mantenimiento",
            icon = Icons.Default.EventAvailable,
            color = Secondary80
        ) { showNewCitaDialog = true }

        // ── Gráficos de Control Diario ────────────────────────────────────────
        DailyChartsCard(stats = todayStats)

        // ── Actividad semanal (últimos 7 días) ────────────────────────────────
        if (chartData.isNotEmpty()) {
            AppointmentChartCard(chartData = chartData)
        }
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
            onDismiss = { showNewCitaDialog = false },
            onSave = { appointment ->
                viewModel.saveAppointment(appointment)
                showNewCitaDialog = false
            }
        )
    }
}

// ─── Tarjeta Citas Pendientes (amarillo) ──────────────────────────────────────
@Composable
fun PendingCard(modifier: Modifier, count: Int, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(YellowGradient))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            Icon(Icons.Default.CalendarMonth, null,
                tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFFFFE082))
            Text("Citas Pendientes",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
            Text("programadas",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f))
        }
    }
}

// ─── Tarjeta Próxima Cita (formato de "Total de Servicios" original) ──────────
@Composable
fun NextAppointmentCard(modifier: Modifier, appointment: Appointment?, currentTime: Long) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Schedule, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text("Próxima cita en:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            if (appointment == null) {
                Text("Sin citas próximas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val minutesLeft = ((appointment.dateTime - currentTime) / 60_000L).toInt()
                val countdownText = when {
                    minutesLeft <= 0   -> "¡Ahora!"
                    minutesLeft < 60   -> "$minutesLeft min"
                    minutesLeft < 1440 -> "${minutesLeft / 60}h ${minutesLeft % 60}min"
                    else               -> "${minutesLeft / 1440}d"
                }
                Text(countdownText, fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                Text(sdf.format(Date(appointment.dateTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(GreenGradient))
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
                    color = Color(0xFF69F0AE))
                Text("realizados hasta hoy",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f))
            }
            Icon(Icons.Default.BarChart, null,
                tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(56.dp))
        }
    }
}

// ─── Gráficos de Control Diario ───────────────────────────────────────────────
@Composable
fun DailyChartsCard(stats: TodayStats) {
    var selectedTab by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Gráficos del Día",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Progreso", style = MaterialTheme.typography.labelSmall) }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Estados", style = MaterialTheme.typography.labelSmall) }
                )
            }
            Spacer(Modifier.height(12.dp))
            if (selectedTab == 0) DailyProgressBars(stats) else DailyPieChart(stats)
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
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ColorCitaChart,
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
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ColorMantChart,
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
                drawArc(color = Color(0xFF4CAF50), startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
                startAngle += sweep
            }
            if (stats.totalPending > 0) {
                val sweep = (stats.totalPending / total) * 360f
                drawArc(color = Color(0xFFFFC107), startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
                startAngle += sweep
            }
            if (stats.totalCancelled > 0) {
                val sweep = (stats.totalCancelled / total) * 360f
                drawArc(color = Color(0xFFF44336), startAngle = startAngle,
                    sweepAngle = sweep, useCenter = true, topLeft = tl, size = sz)
            }
        }

        Spacer(Modifier.width(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LegendDot(color = Color(0xFF4CAF50), label = "Listas: ${stats.totalCompleted}")
            LegendDot(color = Color(0xFFFFC107), label = "Pendientes: ${stats.totalPending}")
            LegendDot(color = Color(0xFFF44336), label = "Canceladas: ${stats.totalCancelled}")
        }
    }
}

// ─── Gráfico de barras semanal ────────────────────────────────────────────────
@Composable
fun AppointmentChartCard(chartData: List<ChartDay>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Actividad — Últimos 7 días",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(color = ColorCitaChart, label = "Cita / Reparación")
                LegendDot(color = ColorMantChart,  label = "Mantenimiento")
            }
            Spacer(Modifier.height(12.dp))

            val maxVal = chartData.maxOf { it.citas + it.mantenimientos }.coerceAtLeast(1)
            val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

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
                        val barW = barGroupW * 0.28f
                        val gap  = barW * 0.2f
                        val maxH = size.height
                        val gridAlpha = 0.18f
                        drawLine(Color.Gray.copy(gridAlpha), Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                        drawLine(Color.Gray.copy(gridAlpha), Offset(0f, maxH / 2), Offset(size.width, maxH / 2), 1.dp.toPx())
                        drawLine(Color.Gray.copy(gridAlpha), Offset(0f, maxH), Offset(size.width, maxH), 1.dp.toPx())

                        chartData.forEachIndexed { i, day ->
                            val groupX = i * barGroupW + barGroupW * 0.1f
                            val citasH = (day.citas.toFloat() / maxVal) * maxH
                            if (citasH > 0f) drawRect(ColorCitaChart, Offset(groupX, maxH - citasH), Size(barW, citasH))
                            val mantH = (day.mantenimientos.toFloat() / maxVal) * maxH
                            if (mantH > 0f) drawRect(ColorMantChart, Offset(groupX + barW + gap, maxH - mantH), Size(barW, mantH))
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
    clients: List<Client>
) {
    val (title, appointments, orders) = when (type) {
        "repair"    -> Triple("En Reparación",         inRepairList.first,  inRepairList.second)
        "today"     -> Triple("Citas Hoy",             todayList,           emptyList<ServiceOrder>())
        "pending"   -> Triple("Citas Pendientes",      pendingList,         emptyList<ServiceOrder>())
        "completed" -> Triple("Servicios Completados", completedList.first, completedList.second)
        else        -> Triple("Servicios",             emptyList<Appointment>(), emptyList<ServiceOrder>())
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
    onDismiss: () -> Unit,
    onSave: (Appointment) -> Unit
) {
    var selectedClient by remember { mutableStateOf<Client?>(null) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var notes         by remember { mutableStateOf("") }
    var serviceType   by remember { mutableStateOf(ServiceType.MAINTENANCE) }
    var equipmentType by remember { mutableStateOf("") }
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    var dateStr  by remember { mutableStateOf("") }
    val equipmentTypes = listOf("Nevera", "Aire Acondicionado", "Lavadora", "Otro")

    val timePicker = TimePickerDialog(context, { _, h, min ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, min)
        dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(calendar.time)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val datePicker = DatePickerDialog(context, { _, y, m, d ->
        calendar.set(y, m, d); timePicker.show()
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.EventAvailable, null, tint = Secondary80)
                Text("Nueva Cita / Mantenimiento", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = clientDropdownExpanded,
                    onExpandedChange = { clientDropdownExpanded = !clientDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedClient?.let {
                            "${it.name}${if (it.lastName.isNotBlank()) " ${it.lastName}" else ""}"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Cliente *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Selecciona un cliente registrado") }
                    )
                    ExposedDropdownMenu(expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false }) {
                        if (clients.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Sin clientes registrados",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant) },
                                onClick = { clientDropdownExpanded = false })
                        } else {
                            clients.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text("${client.name} ${client.lastName}".trim()) },
                                    onClick = { selectedClient = client; clientDropdownExpanded = false },
                                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Secondary80) })
                            }
                        }
                    }
                }
                if (clients.isEmpty()) {
                    Text("⚠️ Ve a Clientes y agrega uno primero.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = dateStr, onValueChange = {},
                    label = { Text("Fecha y hora *") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.fillMaxWidth(), readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.EditCalendar, null, tint = Secondary80)
                        }
                    }
                )
                Text("Motivo:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServiceType.values().forEach { st ->
                        FilterChip(selected = serviceType == st, onClick = { serviceType = st },
                            label = {
                                Text(when (st) {
                                    ServiceType.MAINTENANCE  -> "Mant."
                                    ServiceType.REPAIR       -> "Reparación"
                                    ServiceType.INSTALLATION -> "Instalación"
                                }, style = MaterialTheme.typography.labelSmall)
                            })
                    }
                }
                Text("Tipo de equipo:", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    equipmentTypes.take(2).forEach { et ->
                        FilterChip(selected = equipmentType == et,
                            onClick = { equipmentType = if (equipmentType == et) "" else et },
                            modifier = Modifier.weight(1f),
                            label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    equipmentTypes.drop(2).forEach { et ->
                        FilterChip(selected = equipmentType == et,
                            onClick = { equipmentType = if (equipmentType == et) "" else et },
                            modifier = Modifier.weight(1f),
                            label = { Text(et, style = MaterialTheme.typography.labelSmall) })
                    }
                }
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
                enabled = selectedClient != null && dateStr.isNotBlank()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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
            .clip(RoundedCornerShape(16.dp))
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

@Composable
fun QuickActionButton(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = color,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
