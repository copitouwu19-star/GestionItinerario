package com.gestion.itinerario.ui.quotes

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteItem
import com.gestion.itinerario.data.entity.QuoteStatus
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.ProfileRepository
import com.gestion.itinerario.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class QuoteViewModel @Inject constructor(
    private val quoteRepo: QuoteRepository,
    private val profileRepo: ProfileRepository,
    private val clientRepo: ClientRepository
) : ViewModel() {

    val quotes: StateFlow<List<Quote>> = quoteRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = clientRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _savedQuoteId = MutableStateFlow<String?>(null)
    val savedQuoteId: StateFlow<String?> = _savedQuoteId.asStateFlow()

    fun getQuotesByClient(clientId: String) = quoteRepo.getByClient(clientId)

    fun clearSavedId() { _savedQuoteId.value = null }

    fun createQuote(
        clientId: String,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        equipmentType: String,
        description: String,
        items: List<QuoteItem>,
        validDays: Int,
        onDone: (Quote) -> Unit = {}
    ) = viewModelScope.launch {
        try {
            val year = Calendar.getInstance().get(Calendar.YEAR)
            val existing = quoteRepo.getAll().first()
            val seq = existing.count { it.quoteNumber.contains(year.toString()) } + 1
            val quoteNumber = "COT-$year-${seq.toString().padStart(4, '0')}"

            val validUntil = if (validDays > 0) {
                Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, validDays) }.timeInMillis
            } else 0L

            val quote = Quote(
                quoteNumber   = quoteNumber,
                clientId      = clientId,
                clientName    = clientName,
                clientPhone   = clientPhone,
                clientAddress = clientAddress,
                equipmentType = equipmentType,
                description   = description,
                items         = items,
                totalAmount   = items.sumOf { it.amount },
                status        = QuoteStatus.PENDING,
                validUntil    = validUntil
            )
            val id = quoteRepo.save(quote)
            _savedQuoteId.value = id
            onDone(quote.copy(id = id))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Registra la respuesta del cliente (aprobación o rechazo) junto con su firma digital. */
    fun respondToQuote(
        quote: Quote,
        approved: Boolean,
        signatureBitmap: Bitmap?,
        onDone: () -> Unit = {}
    ) = viewModelScope.launch {
        try {
            val signatureBase64 = signatureBitmap?.let { bmp ->
                val baos = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                android.util.Base64.encodeToString(baos.toByteArray(), android.util.Base64.DEFAULT)
            } ?: ""

            val updated = quote.copy(
                status          = if (approved) QuoteStatus.APPROVED else QuoteStatus.REJECTED,
                clientSignature = signatureBase64,
                respondedAt     = System.currentTimeMillis()
            )
            quoteRepo.update(updated)
            onDone()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun generatePdf(context: Context, quote: Quote): File = withContext(kotlinx.coroutines.Dispatchers.IO) {
        val profile = kotlinx.coroutines.withTimeoutOrNull(5_000L) { profileRepo.getProfile().first() }
            ?: com.gestion.itinerario.data.entity.CompanyProfile()
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
        QuotePdfGenerator.generate(context, quote, profile, logoBitmap)
    }
}
