package com.gestion.itinerario.data.entity

enum class IntervalUnit { WEEKS, MONTHS }

data class MaintenanceReminder(
    val id: String = "",
    val equipmentId: String = "",
    val clientId: String = "",
    val intervalValue: Int = 3,
    val intervalUnit: IntervalUnit = IntervalUnit.MONTHS,
    val intervalMonths: Int = 3,
    val lastServiceDate: Long = 0L,
    val nextServiceDate: Long = 0L,
    val notes: String = "",
    val isActive: Boolean = true
)
