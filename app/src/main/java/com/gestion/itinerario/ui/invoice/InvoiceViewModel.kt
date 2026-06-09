package com.gestion.itinerario.ui.invoice

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.first
import com.gestion.itinerario.data.entity.Invoice
import com.gestion.itinerario.data.entity.InvoiceItem
import com.gestion.itinerario.data.entity.PaymentMethod
import com.gestion.itinerario.data.entity.PaymentStatus
import com.gestion.itinerario.data.repository.InvoiceRepository
import com.gestion.itinerario.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class InvoiceViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val profileRepo: ProfileRepository,
    private val storage: FirebaseStorage
) : ViewModel() {

    val invoices: StateFlow<List<Invoice>> = invoiceRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _savedInvoiceId = MutableStateFlow<String?>(null)
    val savedInvoiceId: StateFlow<String?> = _savedInvoiceId.asStateFlow()

    fun createInvoice(
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
        taxRate: Double = 0.0,
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
        paymentTerms: String = "Contado",
        startDate: Long,
        signatureBitmap: Bitmap?,
        onDone: (invoiceId: String, invoiceNumber: String) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) = viewModelScope.launch {
        try {
            val profile = kotlinx.coroutines.withTimeoutOrNull(8_000L) { profileRepo.getProfile().first() }
                ?: com.gestion.itinerario.data.entity.CompanyProfile()
            val counter = profileRepo.incrementCounter()
            val invoiceNumber = "${profile.invoicePrefix}-${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}-${counter.toString().padStart(4, '0')}"

            val signatureBase64 = signatureBitmap?.let { bmp ->
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
            } ?: ""

            // El monto ingresado es el subtotal (valor del servicio); el impuesto se suma encima
            val taxAmount = totalAmount * (taxRate / 100.0)
            val grandTotal = totalAmount + taxAmount

            val invoice = Invoice(
                invoiceNumber      = invoiceNumber,
                serviceOrderId     = serviceOrderId,
                appointmentId      = appointmentId,
                clientId           = clientId,
                clientName         = clientName,
                clientPhone        = clientPhone,
                clientAddress      = clientAddress,
                equipmentType      = equipmentType,
                serviceDescription = serviceDescription,
                diagnosis          = diagnosis,
                items              = listOf(InvoiceItem(
                    description = serviceDescription,
                    quantity    = 1.0,
                    unit        = "Servicio",
                    unitPrice   = totalAmount,
                    amount      = totalAmount
                )),
                taxRate            = taxRate,
                totalAmount        = grandTotal,
                paymentMethod      = paymentMethod,
                paymentStatus      = paymentStatus,
                paymentTerms       = paymentTerms,
                companyTaxId       = profile.taxId,
                clientSignature    = signatureBase64,
                companyLogoUrl     = profile.logoUrl,
                startDate          = startDate,
                endDate            = System.currentTimeMillis()
            )
            val id = invoiceRepo.save(invoice)
            _savedInvoiceId.value = id
            onDone(id, invoiceNumber)
        } catch (e: Exception) {
            e.printStackTrace()
            onError(e)
        }
    }

    fun getInvoicesByClient(clientId: String) = invoiceRepo.getByClient(clientId)

    fun clearSavedId() { _savedInvoiceId.value = null }

    /** Descarga el logo desde la URL (si existe) y genera el PDF. Retorna el File. */
    suspend fun generatePdf(
        context: android.content.Context,
        invoice: Invoice,
        profile: com.gestion.itinerario.data.entity.CompanyProfile
    ): java.io.File = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val logoBitmap: android.graphics.Bitmap? = if (profile.logoUrl.isNotBlank()) {
            try {
                if (profile.logoUrl.startsWith("data:")) {
                    val b64 = profile.logoUrl.substringAfter(",")
                    val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                    android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } else {
                    val connection = java.net.URL(profile.logoUrl).openConnection() as java.net.HttpURLConnection
                    connection.connectTimeout = 3_000
                    connection.readTimeout   = 3_000
                    connection.instanceFollowRedirects = true
                    connection.inputStream.use { android.graphics.BitmapFactory.decodeStream(it) }
                }
            } catch (_: Exception) { null }
        } else null
        com.gestion.itinerario.ui.invoice.InvoicePdfGenerator.generate(context, invoice, profile, logoBitmap)
    }

    suspend fun uploadPhoto(uri: Uri, uid: String, orderId: String, phase: String): String {
        val fileName = "services/$uid/$orderId/${phase}_${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child(fileName)
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
