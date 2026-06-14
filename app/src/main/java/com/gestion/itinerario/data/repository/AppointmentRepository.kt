package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.remote.CitaApiService
import com.gestion.itinerario.data.remote.toRemota
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(private val api: CitaApiService) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    private var lastUid = ""

    fun getAll(): Flow<List<Appointment>> = flow {
        val currentUid = uid
        if (currentUid.isNotEmpty() && currentUid != lastUid) {
            lastUid = currentUid
            refresh()
        }
        emitAll(_appointments)
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
        return _appointments.value.firstOrNull {
            it.id != excludeId && it.status == AppointmentStatus.SCHEDULED && it.dateTime in from..to
        }
    }

    suspend fun getInRange(from: Long, to: Long): List<Appointment> =
        _appointments.value.filter { it.dateTime in from..to }

    suspend fun getById(id: String): Appointment? =
        _appointments.value.firstOrNull { it.id == id }

    suspend fun save(app: Appointment): String {
        val id = app.id.ifEmpty { UUID.randomUUID().toString() }
        val a = app.copy(id = id)
        try { api.save(a.toRemota(uid)) } catch (_: Exception) {}
        val list = _appointments.value.toMutableList().apply {
            removeAll { it.id == id }
            add(a)
        }
        _appointments.value = list.sortedBy { it.dateTime }
        return id
    }

    suspend fun update(app: Appointment) {
        try { api.update(app.id, app.toRemota(uid)) } catch (_: Exception) {}
        _appointments.value = _appointments.value.map { if (it.id == app.id) app else it }
    }

    suspend fun delete(app: Appointment) {
        try { api.delete(app.id) } catch (_: Exception) {}
        _appointments.value = _appointments.value.filter { it.id != app.id }
    }

    suspend fun refresh() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        try {
            val resp = api.getAll(currentUid)
            if (resp.isSuccessful) {
                _appointments.value = resp.body()?.data?.map { it.toDomain() }
                    ?.sortedBy { it.dateTime } ?: emptyList()
            }
        } catch (_: Exception) {}
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
