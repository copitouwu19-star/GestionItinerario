package com.gestion.itinerario.data.entity

enum class MovementType { ENTRY, EXIT }

data class StockMovement(
    val id: String = "",
    val sparePartId: String = "",
    val type: MovementType = MovementType.ENTRY,
    val quantity: Int = 0,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)
