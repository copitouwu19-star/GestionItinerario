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
        paymentMethod: PaymentMethod,
        paymentStatus: PaymentStatus,
        startDate: Long,
        signatureBitmap: Bitmap?,
        onDone: (invoiceId: String, invoiceNumber: String) -> Unit
    ) = viewModelScope.launch {
        try {
            val profile = profileRepo.getProfile().let { flow ->
                var p = com.gestion.itinerario.data.entity.CompanyProfile()
                flow.first().also { p = it }
                p
            }
            val counter = profileRepo.incrementCounter()
            val invoiceNumber = "${profile.invoicePrefix}-${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)}-${counter.toString().padStart(4, '0')}"

            val signatureBase64 = signatureBitmap?.let { bmp ->
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
            } ?: ""

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
                items              = listOf(InvoiceItem(serviceDescription, totalAmount)),
                totalAmount        = totalAmount,
                paymentMethod      = paymentMethod,
                paymentStatus      = paymentStatus,
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
                val stream = java.net.URL(profile.logoUrl).openStream()
                android.graphics.BitmapFactory.decodeStream(stream)
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
