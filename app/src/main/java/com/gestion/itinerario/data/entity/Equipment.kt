package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EquipmentStatus { AVAILABLE, IN_REPAIR, DELIVERED }

@Entity(tableName = "equipment")
data class Equipment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brand: String,
    val model: String,
    val serial: String,
    val status: EquipmentStatus = EquipmentStatus.AVAILABLE,
    val clientId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
