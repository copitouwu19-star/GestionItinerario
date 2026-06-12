package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.PaymentMethod
import com.gestion.itinerario.data.entity.PaymentStatus
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceSparePart
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("serviceOrders")

    fun getAll(): Flow<List<ServiceOrder>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.orderBy("createdAt").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toServiceOrder() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getByEquipment(id: String): Flow<List<ServiceOrder>> =
        getAll().map { list -> list.filter { it.equipmentId == id } }

    fun getByClient(id: String): Flow<List<ServiceOrder>> =
        getAll().map { list -> list.filter { it.clientId == id } }

    fun getByStatus(status: ServiceStatus): Flow<List<ServiceOrder>> =
        getAll().map { list -> list.filter { it.status == status } }

    fun countActive(): Flow<Int> =
        getAll().map { list -> list.count { it.status != ServiceStatus.COMPLETED } }

    fun countInProgress(): Flow<Int> =
        getAll().map { list -> list.count { it.status == ServiceStatus.IN_PROGRESS } }

    fun countTotal(): Flow<Int> =
        getAll().map { list -> list.count { it.status == ServiceStatus.COMPLETED } }

    fun getSpareParts(orderId: String): Flow<List<ServiceSparePart>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val spCol = col.document(orderId).collection("spareParts")
        val reg = spCol.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { doc ->
                ServiceSparePart(
                    serviceOrderId = orderId,
                    sparePartId = doc.getString("sparePartId") ?: "",
                    quantity = doc.getLong("quantity")?.toInt() ?: 0
                )
            } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun getById(id: String): ServiceOrder? = col.document(id).get().await().toServiceOrder()

    suspend fun save(order: ServiceOrder): String {
        val ref = if (order.id.isEmpty()) col.document() else col.document(order.id)
        ref.set(order.toMap()).await()
        return ref.id
    }

    suspend fun update(order: ServiceOrder) { col.document(order.id).set(order.toMap()).await() }
    suspend fun delete(order: ServiceOrder) { col.document(order.id).delete().await() }

    suspend fun insertSparePart(item: ServiceSparePart) {
        col.document(item.serviceOrderId).collection("spareParts")
            .document(item.sparePartId)
            .set(mapOf("sparePartId" to item.sparePartId, "quantity" to item.quantity))
            .await()
    }

    suspend fun clearSpareParts(orderId: String) {
        val docs = col.document(orderId).collection("spareParts").get().await().documents
        val batch = db.batch()
        docs.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }
}

@Suppress("UNCHECKED_CAST")
private fun ServiceOrder.toMap(): Map<String, Any?> = mapOf(
    "equipmentId"   to equipmentId,
    "clientId"      to clientId,
    "type"          to type.name,
    "description"   to description,
    "diagnosis"     to diagnosis,
    "status"        to status.name,
    "equipmentType" to equipmentType,
    "totalCost"     to totalCost,
    "paymentMethod" to paymentMethod.name,
    "paymentStatus" to paymentStatus.name,
    "photosBefore"  to photosBefore,
    "photosAfter"   to photosAfter,
    "invoiceId"     to invoiceId,
    "createdAt"     to createdAt,
    "completedAt"   to completedAt,
    "warrantyMonths"    to warrantyMonths,
    "warrantyExpiresAt" to warrantyExpiresAt,
    "sourceReminderId"  to sourceReminderId
)

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toServiceOrder(): ServiceOrder? {
    if (!exists()) return null
    return try {
        ServiceOrder(
            id            = id,
            equipmentId   = getString("equipmentId") ?: "",
            clientId      = getString("clientId") ?: "",
            type          = ServiceType.valueOf(getString("type") ?: "MAINTENANCE"),
            description   = getString("description") ?: "",
            diagnosis     = getString("diagnosis") ?: "",
            status        = ServiceStatus.valueOf(getString("status") ?: "PENDING"),
            equipmentType = getString("equipmentType") ?: "",
            totalCost     = getDouble("totalCost") ?: 0.0,
            paymentMethod = runCatching { PaymentMethod.valueOf(getString("paymentMethod") ?: "NONE") }.getOrDefault(PaymentMethod.NONE),
            paymentStatus = runCatching { PaymentStatus.valueOf(getString("paymentStatus") ?: "NONE") }.getOrDefault(PaymentStatus.NONE),
            photosBefore  = (get("photosBefore") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            photosAfter   = (get("photosAfter") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            invoiceId     = getString("invoiceId") ?: "",
            createdAt     = getLong("createdAt") ?: System.currentTimeMillis(),
            completedAt   = getLong("completedAt"),
            warrantyMonths    = (getLong("warrantyMonths") ?: 0L).toInt(),
            warrantyExpiresAt = getLong("warrantyExpiresAt"),
            sourceReminderId  = getString("sourceReminderId") ?: ""
        )
    } catch (e: Exception) { null }
}
