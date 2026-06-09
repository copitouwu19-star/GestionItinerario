package com.gestion.itinerario.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gestion.itinerario.data.entity.Appointment
import com.gestion.itinerario.data.entity.Client
import com.gestion.itinerario.data.entity.ServiceType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Abre WhatsApp con un mensaje de recordatorio de cita pre-redactado para el cliente. */
fun sendAppointmentReminderViaWhatsApp(
    context: Context,
    client: Client,
    appointment: Appointment,
    companyName: String = ""
) {
    val phone = client.phone.filter { it.isDigit() }
    if (phone.isBlank()) return

    val tipo = when (appointment.serviceType) {
        ServiceType.MAINTENANCE  -> "mantenimiento"
        ServiceType.REPAIR       -> "reparación"
        ServiceType.INSTALLATION -> "instalación"
    }
    val sdf = SimpleDateFormat("EEEE d 'de' MMMM 'a las' HH:mm", Locale("es", "ES"))
    val whenText = sdf.format(Date(appointment.dateTime))
    val firma = if (companyName.isNotBlank()) "\n— $companyName" else ""
    val message = "Hola ${client.name.ifBlank { "" }}, te recordamos tu cita de $tipo programada para el $whenText. " +
        "Si necesitas reprogramar, contáctanos respondiendo este mensaje.$firma"

    val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).apply { setPackage("com.whatsapp") }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}

/** Horas restantes hasta la cita (negativo si ya pasó). */
fun hoursUntil(appointment: Appointment): Double =
    (appointment.dateTime - System.currentTimeMillis()) / 3_600_000.0
