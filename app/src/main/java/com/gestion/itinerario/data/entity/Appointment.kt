package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppointmentStatus { SCHEDULED, COMPLETED, CANCELLED }

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val equipmentId: Long? = null,
    val dateTime: Long,
    val serviceType: ServiceType,
    val status: AppointmentStatus = AppointmentStatus.SCHEDULED,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
