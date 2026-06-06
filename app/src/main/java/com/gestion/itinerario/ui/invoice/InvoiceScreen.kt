package com.gestion.itinerario.ui.invoice

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.gestion.itinerario.R
import com.gestion.itinerario.data.entity.*
import com.gestion.itinerario.ui.profile.ProfileViewModel
import com.gestion.itinerario.ui.theme.Primary40
import com.gestion.itinerario.ui.theme.Primary80
import com.gestion.itinerario.ui.theme.StatusCompleted
import java.text.SimpleDateFormat
import java.util.*

private val WhatsAppGreen = Color(0xFF25D366)

@Composable
fun InvoiceCreationDialog(
    serviceOrderId: String = "",
    appointmentId: String = "",
    clientId: String,
    clientName: String,
    clientPhone: String,
    clientAddress: String,
    equipmentType: String,
    serviceDescription: String,
    diagnosis: String,
    totalAmount: Double,
    paymentMethod: PaymentMethod,
    paymentStatus: PaymentStatus,
    startDate: Long,
    onDismiss: () -> Unit,
    onInvoiceCreated: (invoiceId: String, invoiceNumber: String) -> Unit,
    invoiceViewModel: InvoiceViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsStateWithLifecycle()

    var signatureLines by remember { mutableStateOf<List<SignatureLine>>(emptyList()) }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCreating by remember { mutableStateOf(false) }
    var createdInvoice by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Campos editables de pago
    var currentTotalStr     by remember { mutableStateOf(if (totalAmount > 0.0) String.format("%.2f", totalAmount) else "") }
    var currentPayMethod    by remember { mutableStateOf(paymentMethod) }
    var currentPayStatus    by remember { mutableStateOf(paymentStatus) }

    LaunchedEffect(signatureLines) {
        signatureBitmap = if (signatureLines.isNotEmpty())
            signatureLinesToBitmap(signatureLines) else null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Receipt, null, tint = Primary80, modifier = Modifier.size(24.dp))
                        Text("Generar Factura", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                HorizontalDivider()

                if (createdInvoice != null) {
                    // ── Vista de factura creada ────────────────────────────────
                    InvoiceCreatedView(
                        invoiceNumber = createdInvoice!!.second,
                        invoiceId = createdInvoice!!.first,
                        context = context,
                        invoiceViewModel = invoiceViewModel,
                        profile = profile,
                        onClose = { onInvoiceCreated(createdInvoice!!.first, createdInvoice!!.second); onDismiss() }
                    )
                } else {
                    // ── Formulario de factura ──────────────────────────────────
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                        // Resumen del servicio
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Primary80.copy(0.08f))) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Resumen del Servicio", fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.titleSmall)
                                if (clientName.isNotBlank()) InfoLine("Cliente", clientName)
                                if (equipmentType.isNotBlank()) InfoLine("Equipo", equipmentType)
                                InfoLine("Descripción", serviceDescription)
                                if (diagnosis.isNotBlank()) InfoLine("Diagnóstico", diagnosis)
                                if (startDate > 0L) InfoLine("Inicio", sdf.format(Date(startDate)))
                                InfoLine("Finalizado", sdf.format(Date()))
                            }
                        }

                        // ── Pago (editable) ──────────────────────────────────
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.AttachMoney, null,
                                        tint = Primary80, modifier = Modifier.size(18.dp))
                                    Text("Pago", style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold)
                                }
                                OutlinedTextField(
                                    value = currentTotalStr,
                                    onValueChange = { v -> if (v.all { it.isDigit() || it == '.' }) currentTotalStr = v },
                                    label = { Text("Monto (USD)") },
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    placeholder = { Text("0.00") }
                                )
                                Text("Método de pago",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        PaymentMethod.NONE     to "Sin especificar",
                                        PaymentMethod.CASH     to "Efectivo",
                                        PaymentMethod.TRANSFER to "Transferencia"
                                    ).forEach { (m, label) ->
                                        FilterChip(
                                            selected = currentPayMethod == m,
                                            onClick  = { currentPayMethod = m },
                                            label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                                Text("Estado del pago",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        PaymentStatus.NONE    to "Sin especificar",
                                        PaymentStatus.PENDING to "Pendiente",
                                        PaymentStatus.PAID    to "Pagado"
                                    ).forEach { (s, label) ->
                                        FilterChip(
                                            selected = currentPayStatus == s,
                                            onClick  = { currentPayStatus = s },
                                            label    = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }

                        // Empresa en la factura
                        if (profile.companyName.isNotBlank()) {
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Business, null, tint = Primary80, modifier = Modifier.size(20.dp))
                                    Text(profile.companyName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        // Firma digital
                        SignaturePad(
                            modifier = Modifier.fillMaxWidth(),
                            onSignatureChanged = { signatureLines = it }
                        )
                    }

                    HorizontalDivider()
                    // Botón generar
                    Button(
                        onClick = {
                            isCreating = true
                            invoiceViewModel.createInvoice(
                                serviceOrderId     = serviceOrderId,
                                appointmentId      = appointmentId,
                                clientId           = clientId,
                                clientName         = clientName,
                                clientPhone        = clientPhone,
                                clientAddress      = clientAddress,
                                equipmentType      = equipmentType,
                                serviceDescription = serviceDescription,
                                diagnosis          = diagnosis,
                                totalAmount        = currentTotalStr.toDoubleOrNull() ?: 0.0,
                                paymentMethod      = currentPayMethod,
                                paymentStatus      = currentPayStatus,
                                startDate          = startDate,
                                signatureBitmap    = signatureBitmap,
                                onDone = { id, num ->
                                    isCreating = false
                                    createdInvoice = Pair(id, num)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        enabled = !isCreating,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary40)
                    ) {
                        if (isCreating) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.Receipt, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Generar Factura", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceCreatedView(
    invoiceNumber: String,
    invoiceId: String,
    context: android.content.Context,
    invoiceViewModel: InvoiceViewModel,
    profile: CompanyProfile,
    onClose: () -> Unit
) {
    val invoices by invoiceViewModel.invoices.collectAsStateWithLifecycle()
    val invoice = invoices.firstOrNull { it.id == invoiceId }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.size(72.dp).background(StatusCompleted.copy(0.15f), shape = RoundedCornerShape(36.dp)),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.CheckCircle, null, tint = StatusCompleted, modifier = Modifier.size(40.dp))
        }
        Text("¡Factura Generada!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(invoiceNumber, style = MaterialTheme.typography.titleMedium, color = Primary80)

        // Muestra logo de empresa si existe
        if (profile.logoUrl.isNotBlank()) {
            coil.compose.AsyncImage(
                model = profile.logoUrl,
                contentDescription = "Logo empresa",
                modifier = Modifier.height(56.dp),
                contentScale = ContentScale.Fit
            )
        }

        HorizontalDivider()

        // Botones de acción
        Button(
            onClick = {
                invoice?.let { inv ->
                    isGenerating = true
                    scope.launch {
                        try {
                            val file = invoiceViewModel.generatePdf(context, inv, profile)
                            InvoicePdfGenerator.shareViaWhatsApp(context, file)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGenerating,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
        ) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Generando PDF…")
            } else {
                Icon(painterResource(R.drawable.ic_whatsapp), null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Enviar por WhatsApp")
            }
        }

        OutlinedButton(
            onClick = {
                invoice?.let { inv ->
                    isGenerating = true
                    scope.launch {
                        try { invoiceViewModel.generatePdf(context, inv, profile) }
                        catch (_: Exception) {}
                        isGenerating = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isGenerating
        ) {
            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Exportar PDF")
        }

        TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar")
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("$label:", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
