package com.gestion.itinerario.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.IntervalUnit
import com.gestion.itinerario.data.entity.MaintenanceReminder
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.entity.ServiceType
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.ReminderRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repo: ReminderRepository,
    private val clientRepo: ClientRepository,
    private val serviceRepo: ServiceRepository
) : ViewModel() {
    val reminders: StateFlow<List<MaintenanceReminder>> = repo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clients: StateFlow<List<Client>> = clientRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(r: MaintenanceReminder) = viewModelScope.launch {
        val reminderId = repo.save(r)
        serviceRepo.save(ServiceOrder(
            clientId = r.clientId,
            equipmentType = r.equipmentType,
            type = ServiceType.MAINTENANCE,
            description = buildString {
                append("Mantenimiento preventivo")
                if (r.equipmentType.isNotBlank()) append(" – ${r.equipmentType}")
                if (r.notes.isNotBlank()) append(": ${r.notes}")
            },
            status = ServiceStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            sourceReminderId = reminderId
        ))
    }

    fun update(r: MaintenanceReminder) = viewModelScope.launch { repo.update(r) }

    fun delete(r: MaintenanceReminder) = viewModelScope.launch { repo.delete(r) }

    fun markDone(r: MaintenanceReminder) = viewModelScope.launch {
        val cal = Calendar.getInstance()
        val last = cal.timeInMillis
        when (r.intervalUnit) {
            IntervalUnit.WEEKS  -> cal.add(Calendar.WEEK_OF_YEAR, r.intervalValue)
            IntervalUnit.MONTHS -> cal.add(Calendar.MONTH, r.intervalValue)
        }
        val updated = r.copy(
            lastServiceDate = last,
            nextServiceDate = cal.timeInMillis,
            workStatus = "PENDING",
            photosBefore = emptyList(),
            photosDuring = emptyList(),
            photosAfter  = emptyList()
        )
        repo.update(updated)
        // Crear ServiceOrder para el siguiente ciclo
        serviceRepo.save(ServiceOrder(
            clientId = r.clientId,
            equipmentType = r.equipmentType,
            type = ServiceType.MAINTENANCE,
            description = buildString {
                append("Mantenimiento preventivo")
                if (r.equipmentType.isNotBlank()) append(" – ${r.equipmentType}")
                if (r.notes.isNotBlank()) append(": ${r.notes}")
            },
            status = ServiceStatus.PENDING,
            createdAt = System.currentTimeMillis(),
            sourceReminderId = r.id
        ))
    }
}
