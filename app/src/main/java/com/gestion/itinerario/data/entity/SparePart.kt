package com.gestion.itinerario.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spare_parts")
data class SparePart(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val quantity: Int = 0,
    val minStock: Int = 5,
    val unit: String = "unidad",
    val createdAt: Long = System.currentTimeMillis()
)
