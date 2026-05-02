package com.gestion.itinerario.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gestion.itinerario.data.entity.ServiceOrder
import com.gestion.itinerario.data.entity.ServiceSparePart
import com.gestion.itinerario.data.entity.ServiceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceOrderDao {
    @Query("SELECT * FROM service_orders ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE id = :id")
    suspend fun getById(id: Long): ServiceOrder?

    @Query("SELECT * FROM service_orders WHERE equipmentId = :equipmentId ORDER BY createdAt DESC")
    fun getByEquipment(equipmentId: Long): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getByClient(clientId: Long): Flow<List<ServiceOrder>>

    @Query("SELECT * FROM service_orders WHERE status = :status")
    fun getByStatus(status: ServiceStatus): Flow<List<ServiceOrder>>

    @Query("SELECT COUNT(*) FROM service_orders WHERE status != 'COMPLETED'")
    fun countActive(): Flow<Int>

    @Query("SELECT COUNT(*) FROM service_orders")
    fun countTotal(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: ServiceOrder): Long

    @Update
    suspend fun update(order: ServiceOrder)

    @Delete
    suspend fun delete(order: ServiceOrder)
}

@Dao
interface ServiceSparePartDao {
    @Query("SELECT * FROM service_spare_parts WHERE serviceOrderId = :serviceOrderId")
    fun getByOrder(serviceOrderId: Long): Flow<List<ServiceSparePart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ServiceSparePart)

    @Query("DELETE FROM service_spare_parts WHERE serviceOrderId = :serviceOrderId")
    suspend fun deleteByOrder(serviceOrderId: Long)
}
