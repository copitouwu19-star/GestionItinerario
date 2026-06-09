package com.gestion.itinerario.data.entity

enum class QuoteStatus { PENDING, APPROVED, REJECTED }

data class QuoteItem(
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0
)

data class Quote(
    val id: String = "",
    val quoteNumber: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val equipmentType: String = "",
    val description: String = "",
    val items: List<QuoteItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: QuoteStatus = QuoteStatus.PENDING,
    val clientSignature: String = "",
    val validUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null
)
