package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MovementType { ENTRY, EXIT }

@Entity(tableName = "stock_movements")
data class StockMovement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sparePartId: Long,
    val type: MovementType,
    val quantity: Int,
    val notes: String = "",
    val date: Long = System.currentTimeMillis()
)
