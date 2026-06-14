package com.gestion.itinerario.data.remote

import com.google.gson.annotations.SerializedName
import com.gestion.itinerario.data.entity.CompanyProfile

data class PerfilRemoto(
    @SerializedName("firebase_uid") val firebaseUid: String = "",
    @SerializedName("company_name") val companyName: String = "",
    @SerializedName("tax_id") val taxId: String = "",
    @SerializedName("owner_name") val ownerName: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    @SerializedName("logo_url") val logoUrl: String = "",
    @SerializedName("invoice_prefix") val invoicePrefix: String = "FAC",
    @SerializedName("invoice_counter") val invoiceCounter: Int = 0
) {
    fun toDomain() = CompanyProfile(
        companyName = companyName,
        taxId = taxId,
        ownerName = ownerName,
        phone = telefono,
        email = email,
        address = direccion,
        logoUrl = logoUrl,
        invoicePrefix = invoicePrefix,
        invoiceCounter = invoiceCounter
    )
}

fun CompanyProfile.toRemoto(uid: String) = PerfilRemoto(
    firebaseUid = uid,
    companyName = companyName,
    taxId = taxId,
    ownerName = ownerName,
    telefono = phone,
    email = email,
    direccion = address,
    logoUrl = logoUrl,
    invoicePrefix = invoicePrefix,
    invoiceCounter = invoiceCounter
)

data class PerfilResponse(
    val success: Boolean,
    val message: String? = null,
    val data: PerfilRemoto? = null
)
