package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_reminders")
data class MaintenanceReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipmentId: Long,
    val intervalMonths: Int,
    val lastServiceDate: Long,
    val nextServiceDate: Long,
    val notes: String = "",
    val isActive: Boolean = true
)
