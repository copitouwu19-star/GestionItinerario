package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ServiceType { MAINTENANCE, REPAIR, INSTALLATION }
enum class ServiceStatus { PENDING, IN_PROGRESS, COMPLETED }

@Entity(tableName = "service_orders")
data class ServiceOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val equipmentId: Long,
    val clientId: Long,
    val type: ServiceType,
    val description: String,
    val diagnosis: String = "",
    val status: ServiceStatus = ServiceStatus.PENDING,
    val totalCost: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
