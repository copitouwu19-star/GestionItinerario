package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.EquipmentStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("equipment")

    fun getAll(): Flow<List<Equipment>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toEquipment() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getByClient(clientId: String): Flow<List<Equipment>> =
        getAll().map { list -> list.filter { it.clientId == clientId } }

    fun getByStatus(status: EquipmentStatus): Flow<List<Equipment>> =
        getAll().map { list -> list.filter { it.status == status } }

    fun countInRepair(): Flow<Int> =
        getAll().map { list -> list.count { it.status == EquipmentStatus.IN_REPAIR } }

    suspend fun getById(id: String): Equipment? = col.document(id).get().await().toEquipment()

    suspend fun save(equipment: Equipment): String {
        val ref = if (equipment.id.isEmpty()) col.document() else col.document(equipment.id)
        ref.set(equipment.toMap()).await()
        return ref.id
    }

    suspend fun update(equipment: Equipment) { col.document(equipment.id).set(equipment.toMap()).await() }
    suspend fun delete(equipment: Equipment) { col.document(equipment.id).delete().await() }
}

private fun Equipment.toMap(): Map<String, Any?> = mapOf(
    "brand" to brand,
    "model" to model,
    "serial" to serial,
    "status" to status.name,
    "clientId" to clientId,
    "notes" to notes,
    "createdAt" to createdAt
)

private fun com.google.firebase.firestore.DocumentSnapshot.toEquipment(): Equipment? {
    if (!exists()) return null
    return try {
        Equipment(
            id = id,
            brand = getString("brand") ?: "",
            model = getString("model") ?: "",
            serial = getString("serial") ?: "",
            status = EquipmentStatus.valueOf(getString("status") ?: "AVAILABLE"),
            clientId = getString("clientId"),
            notes = getString("notes") ?: "",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis()
        )
    } catch (e: Exception) { null }
}
