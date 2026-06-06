package com.gestion.itinerario.data.entity

enum class EquipmentStatus { AVAILABLE, IN_REPAIR, DELIVERED }

data class Equipment(
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val serial: String = "",
    val status: EquipmentStatus = EquipmentStatus.AVAILABLE,
    val clientId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
