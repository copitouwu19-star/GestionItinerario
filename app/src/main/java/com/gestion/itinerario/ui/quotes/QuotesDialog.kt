package com.gestion.itinerario.ui.quotes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteStatus
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusLowStock
import com.gestion.itinerario.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.*

private val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

internal fun statusInfo(status: QuoteStatus): Pair<Color, String> = when (status) {
    QuoteStatus.PENDING  -> StatusPending to "Pendiente"
    QuoteStatus.APPROVED -> StatusCompleted to "Aprobada"
    QuoteStatus.REJECTED -> StatusLowStock to "Rechazada"
}

private val TABS = listOf("TODAS", "PENDIENTES", "APROBADAS", "RECHAZADAS")

@Composable
fun QuotesDialog(
    onDismiss: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var selectedQuote by remember { mutableStateOf<Quote?>(null) }
    var tabIndex by remember { mutableStateOf(0) }

    val filtered = remember(quotes, tabIndex) {
        when (tabIndex) {
            1 -> quotes.filter { it.status == QuoteStatus.PENDING }
            2 -> quotes.filter { it.status == QuoteStatus.APPROVED }
            3 -> quotes.filter { it.status == QuoteStatus.REJECTED }
            else -> quotes
        }.sortedByDescending { it.createdAt }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFF5F5F5),
            tonalElevation = 0.dp,
            shadowElevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.RequestQuote, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text("Cotizaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    }

                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TABS.forEachIndexed { i, label ->
                            val isSelected = tabIndex == i
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                                            RoundedCornerShape(50)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall,
                                        color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.Transparent,
                                    onClick = { tabIndex = i }
                                ) {
                                    Text(
                                        label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()

                    if (filtered.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.RequestQuote, null, modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            Text("No hay cotizaciones", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered) { quote ->
                                QuoteCard(quote = quote, onClick = { selectedQuote = quote })
                            }
                            item { Spacer(Modifier.height(72.dp)) }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { showCreate = true },
                    icon = { Icon(Icons.Default.Add, null, tint = Color.White) },
                    text = { Text("Nueva cotización", color = Color.White, fontWeight = FontWeight.Bold) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                )
            }
        }
    }

    if (showCreate) {
        QuoteFormDialog(onDismiss = { showCreate = false })
    }
    selectedQuote?.let { quote ->
        QuoteDetailDialog(quote = quote, onDismiss = { selectedQuote = null })
    }
}

@Composable
private fun QuoteCard(quote: Quote, onClick: () -> Unit) {
    val (color, label) = statusInfo(quote.status)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(quote.quoteNumber,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary)
                Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
                    Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(quote.clientName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            if (quote.description.isNotBlank()) {
                Text(quote.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarToday, null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Creada: ${sdf.format(Date(quote.createdAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("$${String.format("%.2f", quote.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("VER DETALLE >",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
