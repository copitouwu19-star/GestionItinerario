package com.gestion.itinerario.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.repository.AppointmentRepository
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.ReminderRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepo: ServiceRepository,
    private val appointmentRepo: AppointmentRepository,
    private val reminderRepo: ReminderRepository,
    val clientRepo: ClientRepository,
    val equipmentRepo: EquipmentRepository
) : ViewModel() {

    val orders: StateFlow<List<ServiceOrder>> = serviceRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Todas las citas (para filtrar en UI según tab) */
    val allAppointments: StateFlow<List<Appointment>> = appointmentRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(order: ServiceOrder) = viewModelScope.launch { serviceRepo.save(order) }
    fun update(order: ServiceOrder) = viewModelScope.launch { serviceRepo.update(order) }
    fun delete(order: ServiceOrder) = viewModelScope.launch { serviceRepo.delete(order) }
    fun updateStatus(order: ServiceOrder, status: ServiceStatus) =
        viewModelScope.launch { serviceRepo.update(order.copy(status = status)) }

    /** SCHEDULED → IN_PROGRESS */
    fun startAppointment(a: Appointment) = viewModelScope.launch {
        appointmentRepo.update(a.copy(status = AppointmentStatus.IN_PROGRESS))
    }

    /** IN_PROGRESS → COMPLETED */
    fun completeAppointment(a: Appointment) = viewModelScope.launch {
        appointmentRepo.update(a.copy(status = AppointmentStatus.COMPLETED))
    }

    fun cancelAppointment(a: Appointment) = viewModelScope.launch {
        appointmentRepo.update(a.copy(status = AppointmentStatus.CANCELLED))
    }

    fun scheduleNextMaintenance(
        equipmentId: String,
        clientId: String,
        intervalValue: Int,
        intervalUnit: IntervalUnit,
        notes: String = ""
    ) = viewModelScope.launch {
        val cal = java.util.Calendar.getInstance()
        val now = cal.timeInMillis
        when (intervalUnit) {
            IntervalUnit.WEEKS  -> cal.add(java.util.Calendar.WEEK_OF_YEAR, intervalValue)
            IntervalUnit.MONTHS -> cal.add(java.util.Calendar.MONTH, intervalValue)
        }
        val approxMonths = when (intervalUnit) {
            IntervalUnit.WEEKS  -> (intervalValue / 4.0).coerceAtLeast(1.0).toInt()
            IntervalUnit.MONTHS -> intervalValue
        }
        reminderRepo.save(MaintenanceReminder(
            equipmentId = equipmentId,
            clientId = clientId,
            intervalValue = intervalValue,
            intervalUnit = intervalUnit,
            intervalMonths = approxMonths,
            lastServiceDate = now,
            nextServiceDate = cal.timeInMillis,
            notes = notes
        ))
    }
}
