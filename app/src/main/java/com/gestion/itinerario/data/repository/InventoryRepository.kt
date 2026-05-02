package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.SparePartDao
import com.gestion.itinerario.data.db.StockMovementDao
import com.gestion.itinerario.data.entity.MovementType
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.data.entity.StockMovement
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val sparePartDao: SparePartDao,
    private val stockMovementDao: StockMovementDao
) {
    fun getAllSpareParts(): Flow<List<SparePart>> = sparePartDao.getAll()
    fun getLowStock(): Flow<List<SparePart>> = sparePartDao.getLowStock()
    fun countLowStock(): Flow<Int> = sparePartDao.countLowStock()
    suspend fun getSparePartById(id: Long): SparePart? = sparePartDao.getById(id)
    suspend fun saveSparePart(sp: SparePart): Long = sparePartDao.insert(sp)
    suspend fun updateSparePart(sp: SparePart) = sparePartDao.update(sp)
    suspend fun deleteSparePart(sp: SparePart) = sparePartDao.delete(sp)

    fun getMovements(sparePartId: Long): Flow<List<StockMovement>> =
        stockMovementDao.getBySpare(sparePartId)

    suspend fun registerMovement(sp: SparePart, type: MovementType, qty: Int, notes: String) {
        val updated = sp.copy(
            quantity = if (type == MovementType.ENTRY) sp.quantity + qty
                       else (sp.quantity - qty).coerceAtLeast(0)
        )
        sparePartDao.update(updated)
        stockMovementDao.insert(StockMovement(sparePartId = sp.id, type = type, quantity = qty, notes = notes))
    }
}
