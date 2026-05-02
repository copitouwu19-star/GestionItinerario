package com.gestion.itinerario.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.AppointmentStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY dateTime ASC")
    fun getAll(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id")
    suspend fun getById(id: Long): Appointment?

    @Query("SELECT * FROM appointments WHERE dateTime >= :startOfDay AND dateTime <= :endOfDay AND status = 'SCHEDULED' ORDER BY dateTime ASC")
    fun getTodayAppointments(startOfDay: Long, endOfDay: Long): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateTime >= :from AND status = 'SCHEDULED' ORDER BY dateTime ASC")
    fun getUpcoming(from: Long): Flow<List<Appointment>>

    @Query("SELECT COUNT(*) FROM appointments WHERE dateTime >= :startOfDay AND dateTime <= :endOfDay AND status = 'SCHEDULED'")
    fun countToday(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: Appointment): Long

    @Update
    suspend fun update(appointment: Appointment)

    @Delete
    suspend fun delete(appointment: Appointment)
}
