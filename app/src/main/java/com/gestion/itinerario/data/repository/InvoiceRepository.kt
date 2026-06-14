package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.Invoice
import com.gestion.itinerario.data.entity.InvoiceItem
import com.gestion.itinerario.data.entity.PaymentMethod
import com.gestion.itinerario.data.entity.PaymentStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("invoices")

    fun getAll(): Flow<List<Invoice>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.orderBy("createdAt").addSnapshotListener { snap, err ->
            if (err != null) { trySend(emptyList()); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toInvoice() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getByClient(clientId: String): Flow<List<Invoice>> =
        getAll().map { list -> list.filter { it.clientId == clientId } }

    suspend fun getById(id: String): Invoice? = col.document(id).get().await().toInvoice()

    suspend fun save(invoice: Invoice): String {
        val ref = if (invoice.id.isEmpty()) col.document() else col.document(invoice.id)
        // Firebase Task sigue corriendo en background aunque se acabe el timeout;
        // Firestore sincronizará cuando haya red. Retornamos el ID sin esperar ACK del servidor.
        try { withTimeout(5_000L) { ref.set(invoice.toMap()).await() } } catch (_: Exception) {}
        return ref.id
    }

    suspend fun delete(invoice: Invoice) {
        try { withTimeout(5_000L) { col.document(invoice.id).delete().await() } } catch (_: Exception) {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun Invoice.toMap(): Map<String, Any?> = mapOf(
    "invoiceNumber"      to invoiceNumber,
    "serviceOrderId"     to serviceOrderId,
    "appointmentId"      to appointmentId,
    "clientId"           to clientId,
    "clientName"         to clientName,
    "clientPhone"        to clientPhone,
    "clientAddress"      to clientAddress,
    "equipmentType"      to equipmentType,
    "serviceDescription" to serviceDescription,
    "diagnosis"          to diagnosis,
    "items"              to items.map {
        mapOf(
            "description" to it.description,
            "quantity"    to it.quantity,
            "unit"        to it.unit,
            "unitPrice"   to it.unitPrice,
            "amount"      to it.amount
        )
    },
    "taxRate"            to taxRate,
    "totalAmount"        to totalAmount,
    "paymentMethod"      to paymentMethod.name,
    "paymentStatus"      to paymentStatus.name,
    "paymentTerms"       to paymentTerms,
    "companyTaxId"       to companyTaxId,
    "clientSignature"    to clientSignature,
    "companyLogoUrl"     to companyLogoUrl,
    "startDate"          to startDate,
    "endDate"            to endDate,
    "createdAt"          to createdAt
)

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toInvoice(): Invoice? {
    if (!exists()) return null
    return try {
        val rawItems = get("items") as? List<Map<String, Any>> ?: emptyList()
        Invoice(
            id                 = id,
            invoiceNumber      = getString("invoiceNumber") ?: "",
            serviceOrderId     = getString("serviceOrderId") ?: "",
            appointmentId      = getString("appointmentId") ?: "",
            clientId           = getString("clientId") ?: "",
            clientName         = getString("clientName") ?: "",
            clientPhone        = getString("clientPhone") ?: "",
            clientAddress      = getString("clientAddress") ?: "",
            equipmentType      = getString("equipmentType") ?: "",
            serviceDescription = getString("serviceDescription") ?: "",
            diagnosis          = getString("diagnosis") ?: "",
            items              = rawItems.map { InvoiceItem(
                description = it["description"] as? String ?: "",
                quantity    = (it["quantity"] as? Number)?.toDouble() ?: 1.0,
                unit        = it["unit"] as? String ?: "Servicio",
                unitPrice   = (it["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                amount      = (it["amount"] as? Number)?.toDouble() ?: 0.0
            )},
            taxRate            = getDouble("taxRate") ?: 0.0,
            totalAmount        = getDouble("totalAmount") ?: 0.0,
            paymentMethod      = runCatching { PaymentMethod.valueOf(getString("paymentMethod") ?: "NONE") }.getOrDefault(PaymentMethod.NONE),
            paymentStatus      = runCatching { PaymentStatus.valueOf(getString("paymentStatus") ?: "NONE") }.getOrDefault(PaymentStatus.NONE),
            paymentTerms       = getString("paymentTerms") ?: "Contado",
            companyTaxId       = getString("companyTaxId") ?: "",
            clientSignature    = getString("clientSignature") ?: "",
            companyLogoUrl     = getString("companyLogoUrl") ?: "",
            startDate          = getLong("startDate") ?: 0L,
            endDate            = getLong("endDate") ?: 0L,
            createdAt          = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}
