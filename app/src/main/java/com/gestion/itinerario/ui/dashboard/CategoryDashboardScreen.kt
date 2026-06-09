package com.gestion.itinerario.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusInRepair
import com.gestion.itinerario.ui.theme.StatusLowStock
import com.gestion.itinerario.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mini-dashboard de una categoría de servicio (Electricidad / Refrigeración / Aire
 * Acondicionado / Otros): muestra el estado actual, actividad de los últimos 7 días
 * e historial reciente, todo filtrado por esa categoría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDashboardScreen(
    stats: CategoryStats,
    onDismiss: () -> Unit
) {
    val category = stats.category

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(category.label, fontWeight = FontWeight.Bold) },
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
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Encabezado con degradado de la categoría ──────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(
                                listOf(category.color, androidx.compose.ui.graphics.lerp(category.color, Color.Black, 0.25f))
                            ))
                            .padding(20.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("ESTADO ACTUAL · ${category.label.uppercase()}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f))
                                Text(stats.total.toString(),
                                    fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("servicios registrados en total",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f))
                                if (stats.revenue > 0.0) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Ingresos generados: $${"%.2f".format(stats.revenue)}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(category.icon, contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(30.dp))
                            }
                        }
                    }

                    // ── Tarjetas de estado: Completados / En Proceso / Pendientes ──
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CategoryStatTile(modifier = Modifier.weight(1f), label = "Completados",
                            value = stats.completed, color = StatusCompleted)
                        CategoryStatTile(modifier = Modifier.weight(1f), label = "En Proceso",
                            value = stats.inProgress, color = StatusInRepair)
                        CategoryStatTile(modifier = Modifier.weight(1f), label = "Pendientes",
                            value = stats.pending, color = StatusPending)
                    }

                    // ── Actividad — Últimos 7 días (barras redondeadas, color de categoría) ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Actividad — Últimos 7 días",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(12.dp))
                            CategoryWeeklyBarChart(points = stats.weekly, barColor = category.color)
                        }
                    }

                    // ── Distribución de estados (pastel) ──────────────────────
                    if (stats.total > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Distribución de Estados",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface)
                                Spacer(Modifier.height(12.dp))
                                CategoryStatusPie(stats = stats)
                            }
                        }
                    }

                    // ── Historial reciente ────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Historial Reciente",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface)
                            if (stats.history.isEmpty()) {
                                Text("Aún no hay servicios registrados en esta categoría.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                val sdf = remember { SimpleDateFormat("dd/MM/yyyy · hh:mm a", Locale.getDefault()) }
                                stats.history.forEachIndexed { idx, item ->
                                    Row(verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                            Text(sdf.format(Date(item.timestamp)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (item.amount != null) {
                                            Text("$${"%.2f".format(item.amount)}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = StatusCompleted)
                                        }
                                    }
                                    if (idx < stats.history.lastIndex) {
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryStatTile(modifier: Modifier, label: String, value: Int, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(value.toString(), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Gráfico de barras de actividad semanal con esquinas superiores redondeadas, en el color de la categoría. */
@Composable
private fun CategoryWeeklyBarChart(points: List<ChartPoint>, barColor: Color) {
    if (points.isEmpty() || points.all { it.value == 0 }) {
        Text("Sin actividad registrada en los últimos 7 días.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    val maxVal = points.maxOf { it.value }.coerceAtLeast(1)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    Column {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val barGroupW = size.width / points.size
            val barW = barGroupW * 0.45f
            val maxH = size.height
            val radius = (barW / 2.5f).coerceAtMost(14.dp.toPx())

            drawLine(trackColor, Offset(0f, maxH), Offset(size.width, maxH), 1.dp.toPx())

            points.forEachIndexed { i, point ->
                val x = i * barGroupW + (barGroupW - barW) / 2f
                val h = (point.value.toFloat() / maxVal) * (maxH - 8.dp.toPx())
                if (h > 0f) {
                    val top = maxH - h
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = x, top = top, right = x + barW, bottom = maxH,
                                topLeftCornerRadius = CornerRadius(radius, radius),
                                topRightCornerRadius = CornerRadius(radius, radius),
                                bottomLeftCornerRadius = CornerRadius.Zero,
                                bottomRightCornerRadius = CornerRadius.Zero
                            )
                        )
                    }
                    val isToday = point.label == "Hoy"
                    drawPath(path, color = if (isToday) barColor else barColor.copy(alpha = 0.45f))
                } else {
                    drawRect(trackColor, Offset(x, maxH - 4.dp.toPx()), Size(barW, 4.dp.toPx()))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            points.forEach { point ->
                Text(point.label, modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor)
            }
        }
    }
}

@Composable
private fun CategoryStatusPie(stats: CategoryStats) {
    val total = (stats.completed + stats.inProgress + stats.pending + stats.cancelled).toFloat()
    if (total <= 0f) return

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

            listOf(
                stats.completed to StatusCompleted,
                stats.inProgress to StatusInRepair,
                stats.pending to StatusPending,
                stats.cancelled to StatusLowStock
            ).forEach { (value, color) ->
                if (value > 0) {
                    val sweep = (value / total) * 360f
                    drawArc(color = color, startAngle = startAngle, sweepAngle = sweep,
                        useCenter = true, topLeft = tl, size = sz)
                    startAngle += sweep
                }
            }
        }
        Spacer(Modifier.width(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            CategoryLegendDot(color = StatusCompleted, label = "Completados: ${stats.completed}")
            CategoryLegendDot(color = StatusInRepair,  label = "En proceso: ${stats.inProgress}")
            CategoryLegendDot(color = StatusPending,   label = "Pendientes: ${stats.pending}")
            if (stats.cancelled > 0) {
                CategoryLegendDot(color = StatusLowStock, label = "Cancelados: ${stats.cancelled}")
            }
        }
    }
}

@Composable
private fun CategoryLegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier
            .size(10.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(color))
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
