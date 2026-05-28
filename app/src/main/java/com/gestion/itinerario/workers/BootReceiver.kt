package com.gestion.itinerario.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gestion.itinerario.data.db.AppDatabase
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Se dispara cuando el dispositivo arranca (BOOT_COMPLETED).
 * Reprograma todas las alarmas de citas futuras para que no se pierdan
 * al reiniciar el teléfono.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase::class.java.let {
                androidx.room.Room.databaseBuilder(context.applicationContext, it, "gestion_itinerario.db")
                    .addMigrations(
                        com.gestion.itinerario.data.db.MIGRATION_2_3,
                        com.gestion.itinerario.data.db.MIGRATION_3_4,
                        com.gestion.itinerario.data.db.MIGRATION_4_5
                    )
                    .build()
            }
            val now = System.currentTimeMillis()
            val upcoming = db.appointmentDao().getInRange(now, now + TimeUnit.DAYS.toMillis(365))
                .filter { it.status == AppointmentStatus.SCHEDULED }

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

            upcoming.forEach { a ->
                val hora = sdf.format(Date(a.dateTime))
                val tipo = when (a.serviceType) {
                    ServiceType.MAINTENANCE -> "Mantenimiento"
                    ServiceType.REPAIR -> "Reparación"
                    ServiceType.INSTALLATION -> "Instalación"
                }
                val clientName = a.notes.substringBefore(" —").ifBlank { "Cliente" }

                // Alarma 1: 24 horas antes
                scheduleAlarm(context, am, (a.id * 10).toInt(),
                    a.dateTime - TimeUnit.HOURS.toMillis(24),
                    "Recordatorio — Mañana", "$tipo con $clientName a las $hora")

                // Alarma 2: 5 horas antes
                scheduleAlarm(context, am, (a.id * 10 + 1).toInt(),
                    a.dateTime - TimeUnit.HOURS.toMillis(5),
                    "Recordatorio — En 5 horas", "$tipo con $clientName a las $hora")

                // Alarma 3: en el momento exacto de la cita (siempre, si la cita es futura)
                scheduleAlarm(context, am, (a.id * 10 + 2).toInt(),
                    a.dateTime,
                    "¡Ahora! $tipo", "$tipo con $clientName programado para ahora ($hora)")
            }
            db.close()
        }
    }

    private fun scheduleAlarm(
        context: Context, am: AlarmManager, rc: Int,
        triggerAt: Long, title: String, message: String
    ) {
        if (triggerAt <= System.currentTimeMillis()) return
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(ReminderAlarmReceiver.EXTRA_TITLE, title)
            putExtra(ReminderAlarmReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderAlarmReceiver.EXTRA_NOTIF_ID, rc)
        }
        val pi = PendingIntent.getBroadcast(
            context, rc, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Intentar alarma exacta; si no hay permiso, usar inexacta como fallback garantizado
        val canUseExact = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> am.canScheduleExactAlarms()
            else -> true
        }
        if (canUseExact) {
            try {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } catch (e: SecurityException) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
