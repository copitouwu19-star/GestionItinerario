package com.gestion.itinerario.data.entity

import androidx.room.Entity

@Entity(tableName = "service_spare_parts", primaryKeys = ["serviceOrderId", "sparePartId"])
data class ServiceSparePart(
    val serviceOrderId: Long,
    val sparePartId: Long,
    val quantity: Int
)
