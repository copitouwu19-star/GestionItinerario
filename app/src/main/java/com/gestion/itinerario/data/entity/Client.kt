package com.gestion.itinerario.data.entity

data class Client(
    val id: String = "",
    val name: String = "",
    val lastName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val clientType: String = "Persona Natural",
    val createdAt: Long = System.currentTimeMillis()
)
