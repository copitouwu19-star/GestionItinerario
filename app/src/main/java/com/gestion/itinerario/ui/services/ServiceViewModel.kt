package com.gestion.itinerario.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceStatus
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepo: ServiceRepository,
    val clientRepo: ClientRepository,
    val equipmentRepo: EquipmentRepository
) : ViewModel() {

    val orders: StateFlow<List<ServiceOrder>> = serviceRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun save(order: ServiceOrder) = viewModelScope.launch { serviceRepo.save(order) }
    fun update(order: ServiceOrder) = viewModelScope.launch { serviceRepo.update(order) }
    fun delete(order: ServiceOrder) = viewModelScope.launch { serviceRepo.delete(order) }
    fun updateStatus(order: ServiceOrder, status: ServiceStatus) =
        viewModelScope.launch { serviceRepo.update(order.copy(status = status)) }
}
