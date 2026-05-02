package com.gestion.itinerario.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gestion.itinerario.data.entity.Equipment
import com.gestion.itinerario.data.entity.EquipmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipment ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun getById(id: Long): Equipment?

    @Query("SELECT * FROM equipment WHERE clientId = :clientId")
    fun getByClient(clientId: Long): Flow<List<Equipment>>

    @Query("SELECT * FROM equipment WHERE status = :status")
    fun getByStatus(status: EquipmentStatus): Flow<List<Equipment>>

    @Query("SELECT COUNT(*) FROM equipment WHERE status = 'IN_REPAIR'")
    fun countInRepair(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(equipment: Equipment): Long

    @Update
    suspend fun update(equipment: Equipment)

    @Delete
    suspend fun delete(equipment: Equipment)
}
