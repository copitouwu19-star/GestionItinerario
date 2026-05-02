package com.gestion.itinerario.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gestion.itinerario.data.entity.MaintenanceReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceReminderDao {
    @Query("SELECT * FROM maintenance_reminders WHERE isActive = 1 ORDER BY nextServiceDate ASC")
    fun getActive(): Flow<List<MaintenanceReminder>>

    @Query("SELECT * FROM maintenance_reminders WHERE equipmentId = :equipmentId")
    fun getByEquipment(equipmentId: Long): Flow<List<MaintenanceReminder>>

    @Query("SELECT * FROM maintenance_reminders WHERE nextServiceDate <= :date AND isActive = 1")
    suspend fun getDue(date: Long): List<MaintenanceReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MaintenanceReminder): Long

    @Update
    suspend fun update(reminder: MaintenanceReminder)

    @Delete
    suspend fun delete(reminder: MaintenanceReminder)
}
