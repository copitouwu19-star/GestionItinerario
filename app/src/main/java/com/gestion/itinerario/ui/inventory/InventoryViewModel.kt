package com.gestion.itinerario.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.EquipmentStatus
import com.gestion.itinerario.data.entity.MovementType
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.data.repository.EquipmentRepository
import com.gestion.itinerario.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val equipmentRepo: EquipmentRepository,
    private val inventoryRepo: InventoryRepository
) : ViewModel() {

    val equipment: StateFlow<List<Equipment>> = equipmentRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val spareParts: StateFlow<List<SparePart>> = inventoryRepo.getAllSpareParts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStock: StateFlow<List<SparePart>> = inventoryRepo.getLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveEquipment(eq: Equipment) = viewModelScope.launch { equipmentRepo.save(eq) }
    fun updateEquipment(eq: Equipment) = viewModelScope.launch { equipmentRepo.update(eq) }
    fun deleteEquipment(eq: Equipment) = viewModelScope.launch { equipmentRepo.delete(eq) }

    fun saveSparePart(sp: SparePart) = viewModelScope.launch { inventoryRepo.saveSparePart(sp) }
    fun updateSparePart(sp: SparePart) = viewModelScope.launch { inventoryRepo.updateSparePart(sp) }
    fun deleteSparePart(sp: SparePart) = viewModelScope.launch { inventoryRepo.deleteSparePart(sp) }

    fun registerMovement(sp: SparePart, type: MovementType, qty: Int, notes: String) =
        viewModelScope.launch { inventoryRepo.registerMovement(sp, type, qty, notes) }
}
