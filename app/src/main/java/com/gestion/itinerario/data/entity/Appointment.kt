package com.gestion.itinerario.data.entity

enum class AppointmentStatus { SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED }

data class Appointment(
    val id: String = "",
    val clientId: String = "",
    val equipmentId: String? = null,
    val dateTime: Long = 0L,
    val serviceType: ServiceType = ServiceType.MAINTENANCE,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String = "",
    val equipmentType: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
