package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("maintenanceReminders")

    fun getActive(): Flow<List<MaintenanceReminder>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.whereEqualTo("isActive", true).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toReminder() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getByEquipment(id: String): Flow<List<MaintenanceReminder>> =
        getActive().map { list -> list.filter { it.equipmentId == id } }

    suspend fun getDue(date: Long): List<MaintenanceReminder> =
        col.whereEqualTo("isActive", true).get().await()
            .documents.mapNotNull { it.toReminder() }
            .filter { it.nextServiceDate <= date }

    suspend fun save(r: MaintenanceReminder) {
        val ref = if (r.id.isEmpty()) col.document() else col.document(r.id)
        ref.set(r.toMap()).await()
    }

    suspend fun update(r: MaintenanceReminder) { col.document(r.id).set(r.toMap()).await() }
    suspend fun delete(r: MaintenanceReminder) { col.document(r.id).delete().await() }
}

private fun MaintenanceReminder.toMap(): Map<String, Any?> = mapOf(
    "equipmentId" to equipmentId,
    "clientId" to clientId,
    "intervalValue" to intervalValue,
    "intervalUnit" to intervalUnit.name,
    "intervalMonths" to intervalMonths,
    "lastServiceDate" to lastServiceDate,
    "nextServiceDate" to nextServiceDate,
    "notes" to notes,
    "isActive" to isActive
)

private fun com.google.firebase.firestore.DocumentSnapshot.toReminder(): MaintenanceReminder? {
    if (!exists()) return null
    return try {
        val unit = runCatching { IntervalUnit.valueOf(getString("intervalUnit") ?: "MONTHS") }.getOrDefault(IntervalUnit.MONTHS)
        val value = getLong("intervalValue")?.toInt() ?: getLong("intervalMonths")?.toInt() ?: 3
        MaintenanceReminder(
            id = id,
            equipmentId = getString("equipmentId") ?: "",
            clientId = getString("clientId") ?: "",
            intervalValue = value,
            intervalUnit = unit,
            intervalMonths = getLong("intervalMonths")?.toInt() ?: 3,
            lastServiceDate = getLong("lastServiceDate") ?: 0L,
            nextServiceDate = getLong("nextServiceDate") ?: 0L,
            notes = getString("notes") ?: "",
            isActive = getBoolean("isActive") ?: true
        )
    } catch (e: Exception) { null }
}
