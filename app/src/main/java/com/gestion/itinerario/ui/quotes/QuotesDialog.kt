package com.gestion.itinerario.ui.quotes

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteStatus
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
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

/** Diálogo principal: lista las cotizaciones existentes y permite crear nuevas o registrar la respuesta del cliente. */
@Composable
fun QuotesDialog(
    onDismiss: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var selectedQuote by remember { mutableStateOf<Quote?>(null) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.RequestQuote, null, tint = Primary80, modifier = Modifier.size(24.dp))
                            Text("Cotizaciones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                    }
                    HorizontalDivider()

                    if (quotes.isEmpty()) {
                        Column(modifier = Modifier.fillMaxSize().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Default.RequestQuote, null, modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.height(12.dp))
                            Text("No hay cotizaciones todavía", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Crea una cotización previa para que el cliente la apruebe antes del servicio",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(quotes.sortedByDescending { it.createdAt }) { quote ->
                                QuoteCard(quote = quote, onClick = { selectedQuote = quote })
                            }
                            item { Spacer(Modifier.height(64.dp)) }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { showCreate = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Nueva cotización") },
                    containerColor = Primary40,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(quote.quoteNumber, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
                    Text(label, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(quote.clientName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (quote.equipmentType.isNotBlank()) {
                Text(quote.equipmentType, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Creada: ${sdf.format(Date(quote.createdAt))}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$${String.format("%.2f", quote.totalAmount)}", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = Primary80)
            }
        }
    }
}
