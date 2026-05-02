package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.MaintenanceReminderDao
import com.gestion.itinerario.data.entity.MaintenanceReminder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderRepository @Inject constructor(private val dao: MaintenanceReminderDao) {
    fun getActive(): Flow<List<MaintenanceReminder>> = dao.getActive()
    fun getByEquipment(id: Long): Flow<List<MaintenanceReminder>> = dao.getByEquipment(id)
    suspend fun getDue(date: Long): List<MaintenanceReminder> = dao.getDue(date)
    suspend fun save(r: MaintenanceReminder) = dao.insert(r)
    suspend fun update(r: MaintenanceReminder) = dao.update(r)
    suspend fun delete(r: MaintenanceReminder) = dao.delete(r)
}
