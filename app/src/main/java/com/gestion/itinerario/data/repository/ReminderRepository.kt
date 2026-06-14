package com.gestion.itinerario.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.remote.MantenimientoApiService
import com.gestion.itinerario.data.remote.toRemoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(private val api: MantenimientoApiService) {

    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val _reminders = MutableStateFlow<List<MaintenanceReminder>>(emptyList())
    private var lastUid = ""

    private fun allReminders(): Flow<List<MaintenanceReminder>> = flow {
        val currentUid = uid
        if (currentUid.isNotEmpty() && currentUid != lastUid) {
            lastUid = currentUid
            refresh()
        }
        emitAll(_reminders)
    }

    fun getActive(): Flow<List<MaintenanceReminder>> =
        allReminders().map { list -> list.filter { it.isActive } }

    fun getByEquipment(id: String): Flow<List<MaintenanceReminder>> =
        getActive().map { list -> list.filter { it.equipmentId == id } }

    suspend fun getDue(date: Long): List<MaintenanceReminder> =
        _reminders.value.filter { it.isActive && it.nextServiceDate <= date }

    suspend fun save(r: MaintenanceReminder): String {
        val id = r.id.ifEmpty { UUID.randomUUID().toString() }
        val reminder = r.copy(id = id)
        try { api.save(reminder.toRemoto(uid)) } catch (_: Exception) {}
        val list = _reminders.value.toMutableList().apply {
            removeAll { it.id == id }
            add(reminder)
        }
        _reminders.value = list.sortedBy { it.nextServiceDate }
        return id
    }

    suspend fun getById(id: String): MaintenanceReminder? =
        _reminders.value.firstOrNull { it.id == id }

    suspend fun update(r: MaintenanceReminder) {
        try { api.update(r.id, r.toRemoto(uid)) } catch (_: Exception) {}
        _reminders.value = _reminders.value.map { if (it.id == r.id) r else it }
    }

    suspend fun delete(r: MaintenanceReminder) {
        try { api.delete(r.id) } catch (_: Exception) {}
        _reminders.value = _reminders.value.filter { it.id != r.id }
    }

    suspend fun refresh() {
        val currentUid = uid
        if (currentUid.isEmpty()) return
        try {
            val resp = api.getAll(currentUid)
            if (resp.isSuccessful) {
                _reminders.value = resp.body()?.data?.map { it.toDomain() }
                    ?.sortedBy { it.nextServiceDate } ?: emptyList()
            }
        } catch (_: Exception) {}
    }
}
