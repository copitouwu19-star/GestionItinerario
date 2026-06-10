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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val MS_24H = 24 * 60 * 60 * 1000L
        private const val MS_5H  =  5 * 60 * 60 * 1000L
    }

    fun schedule(a: Appointment): Boolean {
        val now = System.currentTimeMillis()
        if (a.dateTime <= now) return true

        val am  = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val hora = sdf.format(Date(a.dateTime))
        val tipo = when (a.serviceType) {
            ServiceType.MAINTENANCE  -> "Mantenimiento"
            ServiceType.REPAIR       -> "Reparación"
            ServiceType.INSTALLATION -> "Instalación"
        }
        val clientName = a.notes.substringBefore(" —").ifBlank { "Cliente" }

        // Usamos hash positivo de 30 bits para evitar requestCodes negativos
        val rc = a.id.hashCode() and 0x3FFFFFFF

        // Alarma 1 — 24 horas antes
        scheduleAlarm(am, rc + 2, a.dateTime - MS_24H,
            "Cita mañana — $tipo",
            "$tipo con $clientName mañana a las $hora")

        // Alarma 2 — 5 horas antes
        scheduleAlarm(am, rc + 1, a.dateTime - MS_5H,
            "Cita en 5 horas — $tipo",
            "$tipo con $clientName hoy a las $hora")

        // Alarma 3 — exactamente a la hora de la cita
        scheduleAlarm(am, rc, a.dateTime,
            "¡Ahora! $tipo",
            "$tipo con $clientName a las $hora")

        return true
    }

    fun cancel(appointmentId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val rc = appointmentId.hashCode() and 0x3FFFFFFF
        listOf(rc, rc + 1, rc + 2).forEach { code ->
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
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true
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
        // setAlarmClock: exento de Doze mode, no requiere SCHEDULE_EXACT_ALARM,
        // es el método más confiable para recordatorios exactos
        am.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, null), pi)
    }
}
