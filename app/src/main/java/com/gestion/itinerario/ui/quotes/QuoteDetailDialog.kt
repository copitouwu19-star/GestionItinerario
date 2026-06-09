package com.gestion.itinerario.ui.quotes

import android.graphics.Bitmap
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gestion.itinerario.R
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteStatus
import com.gestion.itinerario.ui.invoice.SignaturePad
import com.gestion.itinerario.ui.invoice.SignatureLine
import com.gestion.itinerario.ui.invoice.signatureLinesToBitmap
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusLowStock
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)
private val detailSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

/** Muestra el detalle de una cotización: ítems, total, estado y acciones (compartir PDF / registrar respuesta). */
@Composable
fun QuoteDetailDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    val current = quotes.firstOrNull { it.id == quote.id } ?: quote

    var isGenerating by remember { mutableStateOf(false) }
    var showApproval by remember { mutableStateOf(false) }
    val (statusColor, statusLabel) = statusInfo(current.status)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(current.quoteNumber, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(current.clientName, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider()

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    Surface(shape = RoundedCornerShape(50), color = statusColor.copy(alpha = 0.15f)) {
                        Text(statusLabel, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium, color = statusColor, fontWeight = FontWeight.Bold)
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Primary80.copy(0.08f))) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            DetailLine("Cliente", current.clientName)
                            if (current.clientPhone.isNotBlank()) DetailLine("Teléfono", current.clientPhone)
                            if (current.clientAddress.isNotBlank()) DetailLine("Dirección", current.clientAddress)
                            if (current.equipmentType.isNotBlank()) DetailLine("Equipo", current.equipmentType)
                            if (current.description.isNotBlank()) DetailLine("Descripción", current.description)
                            DetailLine("Creada", detailSdf.format(Date(current.createdAt)))
                            if (current.validUntil > 0L) DetailLine("Válida hasta", detailSdf.format(Date(current.validUntil)))
                            current.respondedAt?.let { DetailLine("Respondida", detailSdf.format(Date(it))) }
                        }
                    }

                    Text("Detalle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    current.items.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.description} (x${String.format("%.0f", item.quantity)})",
                                style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            Text("$${String.format("%.2f", item.amount)}", style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium)
                        }
                    }
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("Total estimado", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("$${String.format("%.2f", current.totalAmount)}", style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = Primary80)
                    }

                    if (current.status == QuoteStatus.PENDING) {
                        Button(
                            onClick = { showApproval = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary40)
                        ) {
                            Icon(Icons.Default.Draw, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Registrar respuesta del cliente")
                        }
                    }
                }

                HorizontalDivider()
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isGenerating = true
                            scope.launch {
                                try {
                                    val file = viewModel.generatePdf(context, current)
                                    QuotePdfGenerator.shareViaWhatsApp(context, file)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "No se pudo generar el PDF. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show()
                                }
                                isGenerating = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGenerating,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Generando…")
                        } else {
                            Icon(painterResource(R.drawable.ic_whatsapp), null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Enviar al cliente por WhatsApp")
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            isGenerating = true
                            scope.launch {
                                try {
                                    val file = viewModel.generatePdf(context, current)
                                    QuotePdfGenerator.openPdf(context, file)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    android.widget.Toast.makeText(context, "No se pudo generar el PDF. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show()
                                }
                                isGenerating = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isGenerating
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ver / Exportar PDF")
                    }
                }
            }
        }
    }

    if (showApproval) {
        QuoteApprovalDialog(
            quote = current,
            onDismiss = { showApproval = false }
        )
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

/** Captura la decisión del cliente (aprobar/rechazar) junto con su firma digital sobre el dispositivo del técnico. */
@Composable
private fun QuoteApprovalDialog(
    quote: Quote,
    onDismiss: () -> Unit,
    viewModel: QuoteViewModel = hiltViewModel()
) {
    var approve by remember { mutableStateOf(true) }
    var signatureLines by remember { mutableStateOf<List<SignatureLine>>(emptyList()) }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(signatureLines) {
        signatureBitmap = if (signatureLines.isNotEmpty()) signatureLinesToBitmap(signatureLines) else null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Draw, null, tint = Primary80, modifier = Modifier.size(24.dp))
                    Text("Respuesta del cliente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Text("Pide al cliente que firme directamente sobre la pantalla para confirmar su decisión sobre la cotización ${quote.quoteNumber}.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = approve, onClick = { approve = true },
                        leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = StatusCompleted, modifier = Modifier.size(18.dp)) },
                        label = { Text("Aprueba") }
                    )
                    FilterChip(
                        selected = !approve, onClick = { approve = false },
                        leadingIcon = { Icon(Icons.Default.Cancel, null, tint = StatusLowStock, modifier = Modifier.size(18.dp)) },
                        label = { Text("Rechaza") }
                    )
                }

                SignaturePad(modifier = Modifier.fillMaxWidth(), onSignatureChanged = { signatureLines = it })

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), enabled = !isSaving) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            isSaving = true
                            viewModel.respondToQuote(
                                quote = quote, approved = approve, signatureBitmap = signatureBitmap,
                                onDone = { isSaving = false; onDismiss() }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = signatureBitmap != null && !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (approve) StatusCompleted else StatusLowStock
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text(if (approve) "Confirmar aprobación" else "Confirmar rechazo")
                        }
                    }
                }
                if (signatureBitmap == null) {
                    Text("Se requiere la firma del cliente para registrar la respuesta.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
