package com.gestion.itinerario.data.entity

enum class IntervalUnit { WEEKS, MONTHS }

// "MANUAL" = creado desde el botón de mantenimiento
// "AUTO"   = generado al completar un servicio
const val REMINDER_SOURCE_MANUAL = "MANUAL"
const val REMINDER_SOURCE_AUTO   = "AUTO"

data class MaintenanceReminder(
    val id: String = "",
    val equipmentId: String = "",
    val equipmentType: String = "",
    val clientId: String = "",
    val intervalValue: Int = 3,
    val intervalUnit: IntervalUnit = IntervalUnit.MONTHS,
    val intervalMonths: Int = 3,
    val lastServiceDate: Long = 0L,
    val nextServiceDate: Long = 0L,
    val notes: String = "",
    val isActive: Boolean = true,
    val source: String = REMINDER_SOURCE_MANUAL,
    // Flujo de trabajo: "PENDING" | "IN_PROGRESS" | "COMPLETED"
    val workStatus: String = "PENDING",
    val photosBefore: List<String> = emptyList(),
    val photosDuring: List<String> = emptyList(),
    val photosAfter: List<String> = emptyList()
)
