package com.gestion.itinerario.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.entity.REMINDER_SOURCE_AUTO
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType
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

    /** Mantenimientos activos para mostrar en ServicesScreen */
    val reminders: StateFlow<List<MaintenanceReminder>> = reminderRepo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateReminderStatus(r: MaintenanceReminder, status: String) = viewModelScope.launch {
        reminderRepo.update(r.copy(workStatus = status))
    }

    fun updateReminder(r: MaintenanceReminder) = viewModelScope.launch { reminderRepo.update(r) }

    fun deleteReminder(r: MaintenanceReminder) = viewModelScope.launch { reminderRepo.delete(r) }

    fun markReminderDone(r: MaintenanceReminder) = viewModelScope.launch {
        val cal = java.util.Calendar.getInstance()
        val last = cal.timeInMillis
        when (r.intervalUnit) {
            IntervalUnit.WEEKS  -> cal.add(java.util.Calendar.WEEK_OF_YEAR, r.intervalValue)
            IntervalUnit.MONTHS -> cal.add(java.util.Calendar.MONTH, r.intervalValue)
        }
        reminderRepo.update(r.copy(
            lastServiceDate = last,
            nextServiceDate = cal.timeInMillis,
            workStatus = "PENDING",
            photosBefore = emptyList(),
            photosDuring = emptyList(),
            photosAfter  = emptyList()
        ))
    }

    fun save(order: ServiceOrder) = viewModelScope.launch { serviceRepo.save(order) }
    fun update(order: ServiceOrder) = viewModelScope.launch { serviceRepo.update(order) }
    fun delete(order: ServiceOrder) = viewModelScope.launch { serviceRepo.delete(order) }
    fun updateStatus(order: ServiceOrder, status: ServiceStatus) = viewModelScope.launch {
        val updated = if (status == ServiceStatus.COMPLETED && order.status != ServiceStatus.COMPLETED) {
            val completedAt = System.currentTimeMillis()
            val expiresAt = if (order.warrantyMonths > 0) {
                val cal = java.util.Calendar.getInstance().apply {
                    timeInMillis = completedAt
                    add(java.util.Calendar.MONTH, order.warrantyMonths)
                }
                cal.timeInMillis
            } else null
            order.copy(status = status, completedAt = completedAt, warrantyExpiresAt = expiresAt)
        } else {
            order.copy(status = status)
        }
        serviceRepo.update(updated)

        // Si es un mantenimiento vinculado a un reminder, avanzar el ciclo y crear el próximo
        if (status == ServiceStatus.COMPLETED &&
            order.status != ServiceStatus.COMPLETED &&
            order.sourceReminderId.isNotEmpty()) {
            val reminder = reminderRepo.getById(order.sourceReminderId)
            if (reminder != null) {
                val now = System.currentTimeMillis()
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = now }
                when (reminder.intervalUnit) {
                    IntervalUnit.WEEKS  -> cal.add(java.util.Calendar.WEEK_OF_YEAR, reminder.intervalValue)
                    IntervalUnit.MONTHS -> cal.add(java.util.Calendar.MONTH, reminder.intervalValue)
                }
                reminderRepo.update(reminder.copy(
                    lastServiceDate = now,
                    nextServiceDate = cal.timeInMillis
                ))
                serviceRepo.save(ServiceOrder(
                    clientId = reminder.clientId,
                    equipmentType = reminder.equipmentType,
                    type = ServiceType.MAINTENANCE,
                    description = buildString {
                        append("Mantenimiento preventivo")
                        if (reminder.equipmentType.isNotBlank()) append(" – ${reminder.equipmentType}")
                        if (reminder.notes.isNotBlank()) append(": ${reminder.notes}")
                    },
                    status = ServiceStatus.PENDING,
                    createdAt = System.currentTimeMillis(),
                    sourceReminderId = reminder.id
                ))
            }
        }
    }

    /** Busca órdenes completadas del mismo equipo cuya garantía sigue vigente. */
    suspend fun findActiveWarranty(equipmentId: String): ServiceOrder? {
        if (equipmentId.isBlank()) return null
        val now = System.currentTimeMillis()
        return orders.value.filter {
            it.equipmentId == equipmentId &&
                it.status == ServiceStatus.COMPLETED &&
                it.warrantyExpiresAt != null && it.warrantyExpiresAt > now
        }.maxByOrNull { it.completedAt ?: 0L }
    }

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

    fun updateAppointment(a: Appointment) = viewModelScope.launch {
        appointmentRepo.update(a)
    }

    fun scheduleNextMaintenance(
        equipmentId: String,
        clientId: String,
        intervalValue: Int,
        intervalUnit: IntervalUnit,
        notes: String = "",
        equipmentType: String = ""
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
        val reminderId = reminderRepo.save(MaintenanceReminder(
            equipmentId = equipmentId,
            equipmentType = equipmentType,
            clientId = clientId,
            intervalValue = intervalValue,
            intervalUnit = intervalUnit,
            intervalMonths = approxMonths,
            lastServiceDate = now,
            nextServiceDate = cal.timeInMillis,
            notes = notes,
            source = REMINDER_SOURCE_AUTO
        ))
        serviceRepo.save(ServiceOrder(
            clientId = clientId,
            equipmentType = equipmentType,
            type = ServiceType.MAINTENANCE,
            description = buildString {
                append("Mantenimiento preventivo")
                if (equipmentType.isNotBlank()) append(" – $equipmentType")
                if (notes.isNotBlank()) append(": $notes")
            },
            status = ServiceStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            sourceReminderId = reminderId
        ))
    }
}
