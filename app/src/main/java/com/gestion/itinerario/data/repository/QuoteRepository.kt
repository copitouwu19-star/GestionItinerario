package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.Quote
import com.gestion.itinerario.data.entity.QuoteItem
import com.gestion.itinerario.data.entity.QuoteStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("quotes")

    fun getAll(): Flow<List<Quote>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.orderBy("createdAt").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toQuote() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getByClient(clientId: String): Flow<List<Quote>> =
        getAll().map { list -> list.filter { it.clientId == clientId } }

    suspend fun save(quote: Quote): String {
        val ref = if (quote.id.isEmpty()) col.document() else col.document(quote.id)
        try { withTimeout(5_000L) { ref.set(quote.toMap()).await() } } catch (_: Exception) {}
        return ref.id
    }

    suspend fun update(quote: Quote) {
        try { withTimeout(5_000L) { col.document(quote.id).set(quote.toMap()).await() } } catch (_: Exception) {}
    }

    suspend fun delete(quote: Quote) {
        try { withTimeout(5_000L) { col.document(quote.id).delete().await() } } catch (_: Exception) {}
    }
}

@Suppress("UNCHECKED_CAST")
private fun Quote.toMap(): Map<String, Any?> = mapOf(
    "quoteNumber"     to quoteNumber,
    "clientId"        to clientId,
    "clientName"      to clientName,
    "clientPhone"     to clientPhone,
    "clientAddress"   to clientAddress,
    "equipmentType"   to equipmentType,
    "description"     to description,
    "items"           to items.map {
        mapOf(
            "description" to it.description,
            "quantity"    to it.quantity,
            "unitPrice"   to it.unitPrice,
            "amount"      to it.amount
        )
    },
    "totalAmount"     to totalAmount,
    "status"          to status.name,
    "clientSignature" to clientSignature,
    "validUntil"      to validUntil,
    "createdAt"       to createdAt,
    "respondedAt"     to respondedAt
)

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toQuote(): Quote? {
    if (!exists()) return null
    return try {
        val rawItems = get("items") as? List<Map<String, Any>> ?: emptyList()
        Quote(
            id              = id,
            quoteNumber     = getString("quoteNumber") ?: "",
            clientId        = getString("clientId") ?: "",
            clientName      = getString("clientName") ?: "",
            clientPhone     = getString("clientPhone") ?: "",
            clientAddress   = getString("clientAddress") ?: "",
            equipmentType   = getString("equipmentType") ?: "",
            description     = getString("description") ?: "",
            items           = rawItems.map { QuoteItem(
                description = it["description"] as? String ?: "",
                quantity    = (it["quantity"] as? Number)?.toDouble() ?: 1.0,
                unitPrice   = (it["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                amount      = (it["amount"] as? Number)?.toDouble() ?: 0.0
            )},
            totalAmount     = getDouble("totalAmount") ?: 0.0,
            status          = runCatching { QuoteStatus.valueOf(getString("status") ?: "PENDING") }.getOrDefault(QuoteStatus.PENDING),
            clientSignature = getString("clientSignature") ?: "",
            validUntil      = getLong("validUntil") ?: 0L,
            createdAt       = getLong("createdAt") ?: System.currentTimeMillis(),
            respondedAt     = getLong("respondedAt")
        )
    } catch (e: Exception) { null }
}
