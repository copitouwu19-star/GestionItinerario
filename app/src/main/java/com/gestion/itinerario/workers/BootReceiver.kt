package com.gestion.itinerario.workers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.gestion.itinerario.data.entity.AppointmentStatus
import com.gestion.itinerario.data.entity.ServiceType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val snap = FirebaseFirestore.getInstance()
                    .collection("users").document(uid)
                    .collection("appointments")
                    .whereEqualTo("status", AppointmentStatus.SCHEDULED.name)
                    .get().await()

                val upcoming = snap.documents.mapNotNull { doc ->
                    val dateTime = doc.getLong("dateTime") ?: return@mapNotNull null
                    if (dateTime <= now) return@mapNotNull null
                    val id = doc.id
                    val serviceType = try {
                        ServiceType.valueOf(doc.getString("serviceType") ?: "MAINTENANCE")
                    } catch (e: Exception) { ServiceType.MAINTENANCE }
                    val notes = doc.getString("notes") ?: ""
                    Triple(id, dateTime, Pair(serviceType, notes))
                }

                val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

                upcoming.forEach { (id, dateTime, extra) ->
                    val (serviceType, notes) = extra
                    val hora = sdf.format(Date(dateTime))
                    val tipo = when (serviceType) {
                        ServiceType.MAINTENANCE -> "Mantenimiento"
                        ServiceType.REPAIR -> "Reparación"
                        ServiceType.INSTALLATION -> "Instalación"
                    }
                    val clientName = notes.substringBefore(" —").ifBlank { "Cliente" }
                    val rc = id.hashCode()

                    scheduleAlarm(context, am, rc + 1000,
                        dateTime - TimeUnit.HOURS.toMillis(5),
                        "Recordatorio — En 5 horas", "$tipo con $clientName a las $hora")

                    scheduleAlarm(context, am, rc,
                        dateTime,
                        "¡Ahora! $tipo", "$tipo con $clientName programado para ahora ($hora)")
                }
            } catch (e: Exception) {
                // boot re-scheduling is best-effort
            }
        }
    }

    private fun scheduleAlarm(context: Context, am: AlarmManager, rc: Int, triggerAt: Long, title: String, message: String) {
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
        val canExact = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> am.canScheduleExactAlarms()
            else -> true
        }
        if (canExact) {
            try { am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi) }
            catch (e: SecurityException) { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi) }
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }
}
