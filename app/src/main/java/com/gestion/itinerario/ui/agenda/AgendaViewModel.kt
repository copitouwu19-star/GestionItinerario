package com.gestion.itinerario.ui.agenda

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.repository.AppointmentRepository
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.workers.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val repo: AppointmentRepository,
    private val clientRepo: ClientRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _needsExactAlarmPermission = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val needsExactAlarmPermission: SharedFlow<Unit> = _needsExactAlarmPermission.asSharedFlow()

    val appointments: StateFlow<List<Appointment>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcoming: StateFlow<List<Appointment>> = repo.getUpcoming()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = clientRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(a: Appointment) = viewModelScope.launch {
        val id = repo.save(a)
        scheduleAndNotifyIfNeeded(a.copy(id = id))
    }

    fun update(a: Appointment) = viewModelScope.launch {
        alarmScheduler.cancel(a.id)
        repo.update(a)
        if (a.status == AppointmentStatus.SCHEDULED) scheduleAndNotifyIfNeeded(a)
    }

    fun delete(a: Appointment) = viewModelScope.launch {
        alarmScheduler.cancel(a.id)
        repo.delete(a)
    }

    fun cancel(a: Appointment) = viewModelScope.launch {
        alarmScheduler.cancel(a.id)
        repo.update(a.copy(status = AppointmentStatus.CANCELLED))
    }

    fun startAppointment(a: Appointment) = viewModelScope.launch {
        alarmScheduler.cancel(a.id)
        repo.update(a.copy(status = AppointmentStatus.IN_PROGRESS))
    }

    fun complete(a: Appointment) = viewModelScope.launch {
        alarmScheduler.cancel(a.id)
        repo.update(a.copy(status = AppointmentStatus.COMPLETED))
    }

    suspend fun checkConflict(dateTime: Long, excludeId: String = ""): Appointment? =
        repo.checkConflict(dateTime, excludeId)

    private fun scheduleAndNotifyIfNeeded(a: Appointment) {
        val exactScheduled = alarmScheduler.schedule(a)
        if (!exactScheduled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            viewModelScope.launch { _needsExactAlarmPermission.emit(Unit) }
        }
    }
}
