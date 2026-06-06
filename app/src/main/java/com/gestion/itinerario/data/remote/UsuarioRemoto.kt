package com.gestion.itinerario.data.remote

import com.google.gson.annotations.SerializedName

data class UsuarioRemoto(
    val id: Int? = null,
    @SerializedName("firebase_uid") val firebaseUid: String,
    val nombre: String,
    val apellido: String? = null,
    val email: String,
    val telefono: String? = null,
    val rol: String = "tecnico",
    val empresa: String? = null,
    val activo: Int = 1,
    @SerializedName("fecha_creacion") val fechaCreacion: String? = null,
    @SerializedName("ultimo_acceso") val ultimoAcceso: String? = null
)

data class UsuarioResponse(
    val success: Boolean,
    val message: String? = null,
    val data: UsuarioRemoto? = null
)

data class UsuarioListResponse(
    val success: Boolean,
    val data: List<UsuarioRemoto> = emptyList()
)
