package com.gestion.itinerario.data.entity

enum class ServiceType { MAINTENANCE, REPAIR, INSTALLATION }
enum class ServiceStatus { PENDING, IN_PROGRESS, COMPLETED }

enum class PaymentMethod { CASH, TRANSFER, NONE }
enum class PaymentStatus { PAID, PENDING, NONE }

data class ServiceOrder(
    val id: String = "",
    val equipmentId: String = "",
    val clientId: String = "",
    val type: ServiceType = ServiceType.MAINTENANCE,
    val description: String = "",
    val diagnosis: String = "",
    val status: ServiceStatus = ServiceStatus.PENDING,
    val equipmentType: String = "",
    val totalCost: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val paymentStatus: PaymentStatus = PaymentStatus.NONE,
    val photosBefore: List<String> = emptyList(),
    val photosAfter: List<String> = emptyList(),
    val invoiceId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
