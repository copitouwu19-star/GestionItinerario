package com.gestion.itinerario.data.entity

data class InvoiceItem(
    val description: String = "",
    val quantity: Double = 1.0,
    val unit: String = "Servicio",
    val unitPrice: Double = 0.0,
    val amount: Double = 0.0
)

data class Invoice(
    val id: String = "",
    val invoiceNumber: String = "",
    val serviceOrderId: String = "",
    val appointmentId: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientAddress: String = "",
    val equipmentType: String = "",
    val serviceDescription: String = "",
    val diagnosis: String = "",
    val items: List<InvoiceItem> = emptyList(),
    val taxRate: Double = 0.0,
    val totalAmount: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.NONE,
    val paymentStatus: PaymentStatus = PaymentStatus.NONE,
    val paymentTerms: String = "Contado",
    val companyTaxId: String = "",
    val clientSignature: String = "",
    val companyLogoUrl: String = "",
    val startDate: Long = 0L,
    val endDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
