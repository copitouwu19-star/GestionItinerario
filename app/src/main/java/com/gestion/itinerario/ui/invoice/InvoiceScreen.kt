package com.gestion.itinerario.ui.invoice

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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

    var signatureLines  by remember { mutableStateOf<List<SignatureLine>>(emptyList()) }
    var signatureBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCreating      by remember { mutableStateOf(false) }
    var createdInvoice  by remember { mutableStateOf<Pair<String, String>?>(null) }

    var currentTotalStr   by remember { mutableStateOf(if (totalAmount > 0.0) String.format("%.2f", totalAmount) else "") }
    var currentTaxRateStr by remember { mutableStateOf("0") }
    var currentPayMethod  by remember { mutableStateOf(paymentMethod) }
    var currentPayStatus  by remember { mutableStateOf(paymentStatus) }
    var currentPayTerms   by remember { mutableStateOf("Contado") }

    val subtotal     = currentTotalStr.toDoubleOrNull() ?: 0.0
    val taxRateValue = currentTaxRateStr.toDoubleOrNull() ?: 0.0
    val taxAmount    = subtotal * (taxRateValue / 100.0)
    val grandTotal   = subtotal + taxAmount

    LaunchedEffect(signatureLines) {
        signatureBitmap = if (signatureLines.isNotEmpty()) signatureLinesToBitmap(signatureLines) else null
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
            if (createdInvoice != null) {
                InvoiceCreatedView(
                    invoiceNumber = createdInvoice!!.second,
                    invoiceId = createdInvoice!!.first,
                    context = context,
                    invoiceViewModel = invoiceViewModel,
                    profile = profile,
                    onClose = { onInvoiceCreated(createdInvoice!!.first, createdInvoice!!.second); onDismiss() }
                )
            } else {
                Scaffold(
                    containerColor = Color.White,
                    topBar = {
                        TopAppBar(
                            title = { Text("Generar Factura", fontWeight = FontWeight.Bold) },
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
                                            taxRate            = currentTaxRateStr.toDoubleOrNull() ?: 0.0,
                                            paymentMethod      = currentPayMethod,
                                            paymentStatus      = currentPayStatus,
                                            paymentTerms       = currentPayTerms.ifBlank { "Contado" },
                                            startDate          = startDate,
                                            signatureBitmap    = signatureBitmap,
                                            onDone = { id, num ->
                                                isCreating = false
                                                createdInvoice = Pair(id, num)
                                            },
                                            onError = {
                                                isCreating = false
                                                android.widget.Toast.makeText(context,
                                                    "No se pudo generar la factura. Intenta de nuevo.",
                                                    android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    enabled = !isCreating,
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
                                        if (isCreating) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp, color = Color.White)
                                        } else {
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Receipt, null,
                                                    tint = Color.White, modifier = Modifier.size(18.dp))
                                                Text("Generar Factura", color = Color.White,
                                                    fontWeight = FontWeight.SemiBold)
                                            }
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
                        // ── Banner ────────────────────────────────────────
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
                                            .clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Receipt, null,
                                            tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Column {
                                        Text("Generar Factura", style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold)
                                        Text("Confirme los datos de pago y firme para finalizar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        // ── Resumen del servicio ──────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("RESUMEN DEL SERVICIO", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp)
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column(modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    if (clientName.isNotBlank()) InfoLine("Cliente", clientName)
                                    if (equipmentType.isNotBlank()) InfoLine("Equipo", equipmentType)
                                    InfoLine("Descripción", serviceDescription)
                                    if (diagnosis.isNotBlank()) InfoLine("Diagnóstico", diagnosis)
                                    if (startDate > 0L) InfoLine("Inicio", sdf.format(Date(startDate)))
                                    InfoLine("Finalizado", sdf.format(Date()))
                                    if (profile.companyName.isNotBlank()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Business, null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp))
                                            Text(profile.companyName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }

                        // ── Pago ──────────────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PAGO", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp)
                            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                                Column(modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = currentTotalStr,
                                            onValueChange = { v ->
                                                if (v.all { it.isDigit() || it == '.' }) currentTotalStr = v
                                            },
                                            label = { Text("Subtotal") },
                                            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                                            modifier = Modifier.weight(1f), singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            placeholder = { Text("0.00") }
                                        )
                                        OutlinedTextField(
                                            value = currentTaxRateStr,
                                            onValueChange = { v ->
                                                if (v.all { it.isDigit() || it == '.' }) currentTaxRateStr = v
                                            },
                                            label = { Text("IVA %") },
                                            modifier = Modifier.weight(0.6f), singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            placeholder = { Text("0") }
                                        )
                                    }
                                    if (taxRateValue > 0.0) {
                                        Card(shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primary.copy(0.06f))) {
                                            Column(modifier = Modifier.fillMaxWidth().padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                InfoLine("Subtotal", "$${String.format("%.2f", subtotal)}")
                                                InfoLine("IVA (${String.format("%.1f", taxRateValue)}%)", "$${String.format("%.2f", taxAmount)}")
                                                HorizontalDivider()
                                                Row(modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Total", fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.labelMedium)
                                                    Text("$${String.format("%.2f", grandTotal)}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary)
                                                }
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = currentPayTerms,
                                        onValueChange = { currentPayTerms = it },
                                        label = { Text("Condición de pago") },
                                        leadingIcon = { Icon(Icons.Default.Schedule, null) },
                                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                                        placeholder = { Text("Contado, Crédito a 30 días…") }
                                    )
                                    Text("Método de pago", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(PaymentMethod.NONE to "Sin esp.",
                                            PaymentMethod.CASH to "Efectivo",
                                            PaymentMethod.TRANSFER to "Transferencia"
                                        ).forEach { (m, label) ->
                                            FilterChip(selected = currentPayMethod == m,
                                                onClick = { currentPayMethod = m },
                                                label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                                        }
                                    }
                                    Text("Estado del pago", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(PaymentStatus.NONE to "Sin esp.",
                                            PaymentStatus.PENDING to "Pendiente",
                                            PaymentStatus.PAID to "Pagado"
                                        ).forEach { (s, label) ->
                                            FilterChip(selected = currentPayStatus == s,
                                                onClick = { currentPayStatus = s },
                                                label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                                        }
                                    }
                                }
                            }
                        }

                        // ── Firma digital ─────────────────────────────────
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("FIRMA DEL CLIENTE", style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp)
                            SignaturePad(
                                modifier = Modifier.fillMaxWidth(),
                                onSignatureChanged = { signatureLines = it }
                            )
                        }

                        Spacer(Modifier.height(8.dp))
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
            val logoData: Any = remember(profile.logoUrl) {
                if (profile.logoUrl.startsWith("data:")) {
                    val b64 = profile.logoUrl.substringAfter(",")
                    android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                } else profile.logoUrl
            }
            coil.compose.AsyncImage(
                model = logoData,
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
                            android.widget.Toast.makeText(context, "No se pudo generar el PDF. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show()
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
                        try {
                            val file = invoiceViewModel.generatePdf(context, inv, profile)
                            android.widget.Toast.makeText(context, "Factura guardada en el dispositivo. Abriendo…", android.widget.Toast.LENGTH_SHORT).show()
                            InvoicePdfGenerator.openPdf(context, file)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            android.widget.Toast.makeText(context, "No se pudo generar el PDF. Intenta de nuevo.", android.widget.Toast.LENGTH_LONG).show()
                        }
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
