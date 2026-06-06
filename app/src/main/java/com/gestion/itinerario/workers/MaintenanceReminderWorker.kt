package com.gestion.itinerario.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gestion.itinerario.MainActivity
import com.gestion.itinerario.R
import com.gestion.itinerario.data.repository.ReminderRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MaintenanceReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: ReminderRepository
) : CoroutineWorker(context, params) {

    companion object {
        const val CHANNEL_ID = "maintenance_channel"
        const val WORK_NAME = "maintenance_reminder"
    }

    override suspend fun doWork(): Result {
        createChannel()
        val due = repository.getDue(System.currentTimeMillis())
        if (due.isNotEmpty()) {
            showNotification(
                "🔧 Mantenimientos Pendientes",
                "${due.size} equipo(s) requieren mantenimiento"
            )
        }
        return Result.success()
    }

    private fun createChannel() {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recordatorio de Mantenimiento", NotificationManager.IMPORTANCE_HIGH)
            )
        }
    }

    private fun showNotification(title: String, message: String) {
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPi = PendingIntent.getActivity(
            applicationContext, 1002,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPi)
            .build()
        nm.notify(1002, notification)
    }
}
