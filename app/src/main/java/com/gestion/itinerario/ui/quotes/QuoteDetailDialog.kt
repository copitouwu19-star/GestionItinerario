package com.gestion.itinerario.ui.quotes

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.gestion.itinerario.ui.theme.StatusCompleted
import com.gestion.itinerario.ui.theme.StatusLowStock
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)
private val detailSdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val AccentPink = Color(0xFFAD1457)
private val AccentTeal = Color(0xFF00838F)

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
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF5F5F5),
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // Scrollable content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 52.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ── 1. Header card ──────────────────────────────────────
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            color = Color.White, tonalElevation = 0.dp, shadowElevation = 2.dp) {
                            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                                    .background(
                                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)),
                                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                                    ))
                                Column(modifier = Modifier.padding(14.dp).weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Text(current.quoteNumber,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold)
                                        Surface(shape = RoundedCornerShape(50), color = statusColor.copy(0.15f)) {
                                            Text(statusLabel,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = statusColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(current.clientName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // ── 2. Client info card ──────────────────────────────────
                        if (current.clientPhone.isNotBlank() || current.clientAddress.isNotBlank()) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                color = Color.White, tonalElevation = 0.dp, shadowElevation = 2.dp) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                                        .background(AccentPink,
                                            RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)))
                                    Column(modifier = Modifier.padding(14.dp).weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("INFORMACIÓN DEL CLIENTE",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold, color = AccentPink,
                                            letterSpacing = 0.5.sp)
                                        if (current.clientPhone.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Box(modifier = Modifier.size(36.dp)
                                                    .background(AccentPink.copy(0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.Phone, null,
                                                        tint = AccentPink, modifier = Modifier.size(18.dp))
                                                }
                                                Text(current.clientPhone, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                        if (current.clientAddress.isNotBlank()) {
                                            Row(verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Box(modifier = Modifier.size(36.dp)
                                                    .background(AccentPink.copy(0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center) {
                                                    Icon(Icons.Default.LocationOn, null,
                                                        tint = AccentPink, modifier = Modifier.size(18.dp))
                                                }
                                                Text(current.clientAddress, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ── 3. Service details card ──────────────────────────────
                        if (current.equipmentType.isNotBlank() || current.description.isNotBlank()) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                color = Color.White, tonalElevation = 0.dp, shadowElevation = 2.dp) {
                                Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                    Box(modifier = Modifier.width(5.dp).fillMaxHeight()
                                        .background(AccentTeal,
                                            RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)))
                                    Column(modifier = Modifier.padding(14.dp).weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("DETALLES DEL SERVICIO",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold, color = AccentTeal,
                                            letterSpacing = 0.5.sp)
                                        if (current.equipmentType.isNotBlank()) {
                                            Text(buildAnnotatedString {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Equipo: ") }
                                                append(current.equipmentType)
                                            }, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        if (current.description.isNotBlank()) {
                                            Text(buildAnnotatedString {
                                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Notas: ") }
                                                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(current.description) }
                                            }, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        // ── 4. Cost breakdown card ───────────────────────────────
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            color = Color.White, tonalElevation = 0.dp, shadowElevation = 2.dp) {
                            Column(modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Desglose de Costos",
                                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                current.items.forEach { item ->
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)) {
                                            Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                            Text("${item.description} (x${String.format("%.0f", item.quantity)})",
                                                style = MaterialTheme.typography.bodyMedium)
                                        }
                                        Text("$${String.format("%.2f", item.amount)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
                                Row(modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text("Total estimado",
                                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("$${String.format("%.2f", current.totalAmount)}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        // ── 5. Timeline row ──────────────────────────────────────
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                            color = Color.White, tonalElevation = 0.dp, shadowElevation = 2.dp) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf(
                                    "CREADA" to detailSdf.format(Date(current.createdAt)),
                                    "VÁLIDA" to if (current.validUntil > 0L) detailSdf.format(Date(current.validUntil)) else "—",
                                    "RESPONDIDA" to (current.respondedAt?.let { detailSdf.format(Date(it)) } ?: "—")
                                ).forEachIndexed { i, (title, value) ->
                                    Column(modifier = Modifier.weight(1f).padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(title, style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Bold)
                                        Text(value, style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium)
                                    }
                                    if (i < 2) {
                                        VerticalDivider(
                                            modifier = Modifier.height(48.dp).align(Alignment.CenterVertically),
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }
                            }
                        }

                        // ── 6. Registrar respuesta (solo PENDING) ────────────────
                        if (current.status == QuoteStatus.PENDING) {
                            Button(
                                onClick = { showApproval = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Draw, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Registrar respuesta del cliente", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // ── Bottom action buttons (fixed) ────────────────────────────
                    Column(modifier = Modifier
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                isGenerating = true
                                scope.launch {
                                    try {
                                        val file = viewModel.generatePdf(context, current)
                                        QuotePdfGenerator.shareViaWhatsApp(context, file)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        android.widget.Toast.makeText(context,
                                            "No se pudo generar el PDF. Intenta de nuevo.",
                                            android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    isGenerating = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            enabled = !isGenerating,
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Generando…", color = Color.White, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(painterResource(R.drawable.ic_whatsapp), null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Enviar al cliente por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
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
                                        android.widget.Toast.makeText(context,
                                            "No se pudo generar el PDF. Intenta de nuevo.",
                                            android.widget.Toast.LENGTH_LONG).show()
                                    }
                                    isGenerating = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50),
                            enabled = !isGenerating
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Ver / Exportar PDF", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // X close button (floating top right)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                ) {
                    Icon(Icons.Default.Close, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showApproval) {
        QuoteApprovalDialog(quote = current, onDismiss = { showApproval = false })
    }
}

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
            color = Color.White,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Draw, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
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
