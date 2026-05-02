package com.gestion.itinerario.ui.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgendaViewModel @Inject constructor(
    private val repo: AppointmentRepository
) : ViewModel() {
    val appointments: StateFlow<List<Appointment>> = repo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val upcoming: StateFlow<List<Appointment>> = repo.getUpcoming()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(a: Appointment) = viewModelScope.launch { repo.save(a) }
    fun update(a: Appointment) = viewModelScope.launch { repo.update(a) }
    fun delete(a: Appointment) = viewModelScope.launch { repo.delete(a) }
    fun cancel(a: Appointment) = viewModelScope.launch { repo.update(a.copy(status = AppointmentStatus.CANCELLED)) }
    fun complete(a: Appointment) = viewModelScope.launch { repo.update(a.copy(status = AppointmentStatus.COMPLETED)) }
}
