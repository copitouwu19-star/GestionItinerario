package com.gestion.itinerario.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.repository.ClientRepository
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientRepo: ClientRepository,
    private val equipmentRepo: EquipmentRepository,
    private val serviceRepo: ServiceRepository
) : ViewModel() {

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search

    val clients: StateFlow<List<Client>> = _search
        .debounce(300)
        .flatMapLatest { q -> if (q.isBlank()) clientRepo.getAll() else clientRepo.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearch(q: String) { _search.value = q }
    fun save(c: Client) = viewModelScope.launch { clientRepo.save(c) }
    fun update(c: Client) = viewModelScope.launch { clientRepo.update(c) }
    fun delete(c: Client) = viewModelScope.launch { clientRepo.delete(c) }

    fun getEquipmentForClient(clientId: Long): Flow<List<Equipment>> = equipmentRepo.getByClient(clientId)
    fun getServicesForClient(clientId: Long): Flow<List<ServiceOrder>> = serviceRepo.getByClient(clientId)
}
