package com.gestion.itinerario.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.repository.AppointmentRepository
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.InventoryRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    equipmentRepo: EquipmentRepository,
    private val serviceRepo: ServiceRepository,
    inventoryRepo: InventoryRepository,
    private val appointmentRepo: AppointmentRepository
) : ViewModel() {
    val inRepairCount: StateFlow<Int> = equipmentRepo.countInRepair()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val todayAppointments: StateFlow<Int> = appointmentRepo.countToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val lowStockCount: StateFlow<Int> = inventoryRepo.countLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val activeServices: StateFlow<Int> = serviceRepo.countActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalServices: StateFlow<Int> = serviceRepo.countTotal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun saveOrder(order: ServiceOrder) = viewModelScope.launch { serviceRepo.save(order) }
    fun saveAppointment(appointment: Appointment) = viewModelScope.launch { appointmentRepo.save(appointment) }
}
