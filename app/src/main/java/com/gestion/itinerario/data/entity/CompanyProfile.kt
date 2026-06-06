package com.gestion.itinerario.data.entity

data class CompanyProfile(
    val companyName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val logoUrl: String = "",
    val invoicePrefix: String = "FAC",
    val invoiceCounter: Int = 0
)
