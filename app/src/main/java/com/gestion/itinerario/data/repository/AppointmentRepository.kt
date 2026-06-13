package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.ServiceType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(private val db: FirebaseFirestore) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val col get() = db.collection("users").document(uid).collection("appointments")

    fun getAll(): Flow<List<Appointment>> = callbackFlow {
        if (uid.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val reg = col.orderBy("dateTime").addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.documents?.mapNotNull { it.toAppointment() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    fun getUpcoming(from: Long = System.currentTimeMillis()): Flow<List<Appointment>> =
        getAll().map { list -> list.filter { it.dateTime >= from && it.status == AppointmentStatus.SCHEDULED } }

    fun getTodayAppointments(): Flow<List<Appointment>> = getAll().map { list ->
        val (start, end) = todayRange()
        list.filter { it.dateTime in start..end }
    }

    fun countToday(): Flow<Int> = getTodayAppointments().map { it.size }
    fun countUpcoming(): Flow<Int> = getUpcoming().map { it.size }
    fun countPending(): Flow<Int> = getAll().map { list -> list.count { it.status == AppointmentStatus.SCHEDULED } }
    fun countInProgress(): Flow<Int> = getAll().map { list -> list.count { it.status == AppointmentStatus.IN_PROGRESS } }
    fun countCompleted(): Flow<Int> = getAll().map { list -> list.count { it.status == AppointmentStatus.COMPLETED } }
    fun countCompletedToday(): Flow<Int> = getTodayAppointments().map { list -> list.count { it.status == AppointmentStatus.COMPLETED } }

    suspend fun checkConflict(dateTime: Long, excludeId: String = ""): Appointment? {
        val from = dateTime - TimeUnit.MINUTES.toMillis(1)
        val to = dateTime + TimeUnit.MINUTES.toMillis(1)
        return col.get().await().documents.mapNotNull { it.toAppointment() }
            .firstOrNull { it.id != excludeId && it.status == AppointmentStatus.SCHEDULED && it.dateTime in from..to }
    }

    suspend fun getInRange(from: Long, to: Long): List<Appointment> =
        col.get().await().documents.mapNotNull { it.toAppointment() }.filter { it.dateTime in from..to }

    suspend fun getById(id: String): Appointment? = col.document(id).get().await().toAppointment()

    suspend fun save(app: Appointment): String {
        val ref = if (app.id.isEmpty()) col.document() else col.document(app.id)
        // Timeout igual que Invoice/Quote: Firestore escribe en caché local aunque el await no retorne
        try { withTimeout(5_000L) { ref.set(app.toMap()).await() } } catch (_: Exception) {}
        return ref.id
    }

    suspend fun update(app: Appointment) {
        try { withTimeout(5_000L) { col.document(app.id).set(app.toMap()).await() } } catch (_: Exception) {}
    }
    suspend fun delete(app: Appointment) {
        try { withTimeout(5_000L) { col.document(app.id).delete().await() } } catch (_: Exception) {}
    }

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
        return Pair(start, cal.timeInMillis)
    }
}

private fun Appointment.toMap(): Map<String, Any?> = mapOf(
    "clientId" to clientId,
    "equipmentId" to equipmentId,
    "dateTime" to dateTime,
    "serviceType" to serviceType.name,
    "status" to status.name,
    "notes" to notes,
    "equipmentType" to equipmentType,
    "createdAt" to createdAt,
    "photosBefore" to photosBefore,
    "photosDuring" to photosDuring,
    "photosAfter" to photosAfter,
    "completedAt" to completedAt,
    "attendanceStatus" to attendanceStatus
)

@Suppress("UNCHECKED_CAST")
private fun com.google.firebase.firestore.DocumentSnapshot.toAppointment(): Appointment? {
    if (!exists()) return null
    return try {
        Appointment(
            id = id,
            clientId = getString("clientId") ?: "",
            equipmentId = getString("equipmentId"),
            dateTime = getLong("dateTime") ?: 0L,
            serviceType = ServiceType.valueOf(getString("serviceType") ?: "MAINTENANCE"),
            status = AppointmentStatus.valueOf(getString("status") ?: "SCHEDULED"),
            notes = getString("notes") ?: "",
            equipmentType = getString("equipmentType") ?: "",
            createdAt = getLong("createdAt") ?: System.currentTimeMillis(),
            photosBefore = (get("photosBefore") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            photosDuring = (get("photosDuring") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            photosAfter  = (get("photosAfter")  as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
            completedAt      = getLong("completedAt"),
            attendanceStatus = getString("attendanceStatus") ?: ""
        )
    } catch (e: Exception) { null }
}
