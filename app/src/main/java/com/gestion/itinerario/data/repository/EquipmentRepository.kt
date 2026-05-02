package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.EquipmentDao
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.EquipmentStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EquipmentRepository @Inject constructor(private val dao: EquipmentDao) {
    fun getAll(): Flow<List<Equipment>> = dao.getAll()
    fun getByClient(clientId: Long): Flow<List<Equipment>> = dao.getByClient(clientId)
    fun getByStatus(status: EquipmentStatus): Flow<List<Equipment>> = dao.getByStatus(status)
    fun countInRepair(): Flow<Int> = dao.countInRepair()
    suspend fun getById(id: Long): Equipment? = dao.getById(id)
    suspend fun save(equipment: Equipment): Long = dao.insert(equipment)
    suspend fun update(equipment: Equipment) = dao.update(equipment)
    suspend fun delete(equipment: Equipment) = dao.delete(equipment)
}
