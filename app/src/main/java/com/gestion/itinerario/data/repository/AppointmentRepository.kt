package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.db.AppointmentDao
import com.gestion.itinerario.data.entity.Appointment
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppointmentRepository @Inject constructor(private val dao: AppointmentDao) {
    fun getAll(): Flow<List<Appointment>> = dao.getAll()
    fun getUpcoming(from: Long = System.currentTimeMillis()): Flow<List<Appointment>> = dao.getUpcoming(from)
    fun getTodayAppointments(): Flow<List<Appointment>> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return dao.getTodayAppointments(start, end)
    }
    fun countToday(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return dao.countToday(start, end)
    }
    suspend fun getById(id: Long): Appointment? = dao.getById(id)
    suspend fun save(app: Appointment) = dao.insert(app)
    suspend fun update(app: Appointment) = dao.update(app)
    suspend fun delete(app: Appointment) = dao.delete(app)
}
