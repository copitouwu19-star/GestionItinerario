package com.gestion.itinerario.data.entity

enum class ItemCategory { SPARE_PART, TOOL }

data class SparePart(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val quantity: Int = 0,
    val minStock: Int = 5,
    val unit: String = "unidad",
    val category: ItemCategory = ItemCategory.SPARE_PART,
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
