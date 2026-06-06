package com.gestion.itinerario.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.ServiceType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun schedule(a: Appointment): Boolean {
        val now = System.currentTimeMillis()
        if (a.dateTime <= now) return true

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date(a.dateTime))
        val tipo = when (a.serviceType) {
            ServiceType.MAINTENANCE  -> "Mantenimiento"
            ServiceType.REPAIR       -> "Reparación"
            ServiceType.INSTALLATION -> "Instalación"
        }
        val clientName = a.notes.substringBefore(" —").ifBlank { "Cliente" }
        val timeRemainingMs = a.dateTime - now
        val rc = a.id.hashCode()

        scheduleAlarm(am, rc, a.dateTime, "¡Ahora! $tipo", "$tipo con $clientName programado para ahora ($hora)")

        if (timeRemainingMs > TimeUnit.HOURS.toMillis(5)) {
            scheduleAlarm(
                am, rc + 1000,
                a.dateTime - TimeUnit.HOURS.toMillis(5),
                "Recordatorio — En 5 horas",
                "$tipo con $clientName a las $hora"
            )
        }

        return true
    }

    fun cancel(appointmentId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val rc = appointmentId.hashCode()
        listOf(rc, rc + 1000).forEach { code ->
            val intent = Intent(context, ReminderAlarmReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, code, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { am.cancel(it) }
        }
    }

    fun canScheduleExact(): Boolean {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return canScheduleExact(am)
    }

    private fun canScheduleExact(am: AlarmManager): Boolean = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> am.canScheduleExactAlarms()
        else -> true
    }

    private fun scheduleAlarm(am: AlarmManager, requestCode: Int, triggerAt: Long, title: String, message: String) {
        if (triggerAt <= System.currentTimeMillis()) return
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, title)
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderAlarmReceiver.EXTRA_NOTIF_ID, requestCode)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // setAlarmClock dispara exactamente en triggerAt sin requerir SCHEDULE_EXACT_ALARM
        // y no está sujeto a los retrasos de Doze mode (setAndAllowWhileIdle puede tardar ~30 min)
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, null), pi)
    }
}
