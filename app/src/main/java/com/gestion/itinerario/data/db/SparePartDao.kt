package com.gestion.itinerario.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gestion.itinerario.data.entity.SparePart
import com.gestion.itinerario.data.entity.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface SparePartDao {
    @Query("SELECT * FROM spare_parts ORDER BY name ASC")
    fun getAll(): Flow<List<SparePart>>

    @Query("SELECT * FROM spare_parts WHERE id = :id")
    suspend fun getById(id: Long): SparePart?

    @Query("SELECT * FROM spare_parts WHERE quantity < minStock OR quantity <= CAST(minStock * 1.2 AS INTEGER)")
    fun getLowStock(): Flow<List<SparePart>>

    @Query("SELECT COUNT(*) FROM spare_parts WHERE quantity < minStock")
    fun countLowStock(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sparePart: SparePart): Long

    @Update
    suspend fun update(sparePart: SparePart)

    @Delete
    suspend fun delete(sparePart: SparePart)
}

@Dao
interface StockMovementDao {
    @Query("SELECT * FROM stock_movements WHERE sparePartId = :sparePartId ORDER BY date DESC")
    fun getBySpare(sparePartId: Long): Flow<List<StockMovement>>

    @Insert
    suspend fun insert(movement: StockMovement)
}
