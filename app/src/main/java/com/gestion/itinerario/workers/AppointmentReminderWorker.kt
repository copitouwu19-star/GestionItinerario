package com.gestion.itinerario.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gestion.itinerario.R
import com.gestion.itinerario.data.repository.AppointmentRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AppointmentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: AppointmentRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "appointments_channel"
        const val WORK_NAME = "appointment_reminder"
    }

    override suspend fun doWork(): Result {
        createChannel()
        val upcoming = mutableListOf<String>()
        repository.getTodayAppointments().collect { list ->
            list.forEach { upcoming.add("Cita: ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(it.dateTime))}") }
        }
        if (upcoming.isNotEmpty()) {
            showNotification("📅 Citas de hoy", "${upcoming.size} cita(s) programada(s) para hoy")
        }
        return Result.success()
    }

    private fun createChannel() {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recordatorio de Citas", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    private fun showNotification(title: String, message: String) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        nm.notify(1001, notification)
    }
}
