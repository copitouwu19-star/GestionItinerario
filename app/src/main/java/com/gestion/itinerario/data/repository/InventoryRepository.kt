package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.ItemCategory
import com.gestion.itinerario.data.entity.MovementType
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.data.entity.StockMovement
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val partsCol get() = db.collection("users").document(uid).collection("spareParts")
    private val movementsCol get() = db.collection("users").document(uid).collection("stockMovements")

    fun getAllSpareParts(): Flow<List<SparePart>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = partsCol.orderBy("name").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toSparePart() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getLowStock(): Flow<List<SparePart>> =
        getAllSpareParts().map { list -> list.filter { it.quantity < it.minStock || it.quantity <= (it.minStock * 1.2).toInt() } }

    fun countLowStock(): Flow<Int> =
        getAllSpareParts().map { list -> list.count { it.quantity < it.minStock } }

    fun getMovements(sparePartId: String): Flow<List<StockMovement>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = movementsCol.whereEqualTo("sparePartId", sparePartId)
            .orderBy("date").addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.documents?.mapNotNull { it.toStockMovement() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun getSparePartById(id: String): SparePart? = partsCol.document(id).get().await().toSparePart()

    suspend fun saveSparePart(sp: SparePart): String {
        val ref = if (sp.id.isEmpty()) partsCol.document() else partsCol.document(sp.id)
        ref.set(sp.toMap()).await()
        return ref.id
    }

    suspend fun updateSparePart(sp: SparePart) { partsCol.document(sp.id).set(sp.toMap()).await() }
    suspend fun deleteSparePart(sp: SparePart) { partsCol.document(sp.id).delete().await() }

    suspend fun registerMovement(sp: SparePart, type: MovementType, qty: Int, notes: String) {
        val newQty = if (type == MovementType.ENTRY) sp.quantity + qty else (sp.quantity - qty).coerceAtLeast(0)
        partsCol.document(sp.id).set(sp.copy(quantity = newQty).toMap()).await()
        val movRef = movementsCol.document()
        movRef.set(
            mapOf(
                "sparePartId" to sp.id,
                "type" to type.name,
                "quantity" to qty,
                "notes" to notes,
                "date" to System.currentTimeMillis()
            )
        ).await()
    }
}

private fun SparePart.toMap(): Map<String, Any?> = mapOf(
    "name"        to name,
    "description" to description,
    "quantity"    to quantity,
    "minStock"    to minStock,
    "unit"        to unit,
    "category"    to category.name,
    "photoUrl"    to photoUrl,
    "createdAt"   to createdAt
)

private fun com.google.firebase.firestore.DocumentSnapshot.toSparePart(): SparePart? {
    if (!exists()) return null
    return try {
        SparePart(
            id          = id,
            name        = getString("name") ?: "",
            description = getString("description") ?: "",
            quantity    = getLong("quantity")?.toInt() ?: 0,
            minStock    = getLong("minStock")?.toInt() ?: 5,
            unit        = getString("unit") ?: "unidad",
            category    = runCatching { ItemCategory.valueOf(getString("category") ?: "SPARE_PART") }.getOrDefault(ItemCategory.SPARE_PART),
            photoUrl    = getString("photoUrl") ?: "",
            createdAt   = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}

private fun com.google.firebase.firestore.DocumentSnapshot.toStockMovement(): StockMovement? {
    if (!exists()) return null
    return try {
        StockMovement(
            id = id,
            sparePartId = getString("sparePartId") ?: "",
            type = MovementType.valueOf(getString("type") ?: "ENTRY"),
            quantity = getLong("quantity")?.toInt() ?: 0,
            notes = getString("notes") ?: "",
            date = getLong("date") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}
