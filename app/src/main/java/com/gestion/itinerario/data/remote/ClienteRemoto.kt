package com.gestion.itinerario.data.remote

import com.google.gson.annotations.SerializedName
import com.gestion.itinerario.data.entity.Client

data class ClienteRemoto(
    val id: String = "",
    @SerializedName("tecnico_uid")    val tecnicoUid: String = "",
    @SerializedName("tipo_cliente")   val tipoCliente: String = "natural",
    val nombre: String = "",
    val apellido: String = "",
    @SerializedName("nombre_empresa") val nombreEmpresa: String = "",
    val telefono: String = "",
    val email: String = "",
    val direccion: String = "",
    val notas: String = "",
    val activo: Int = 1,
    @SerializedName("fecha_creacion") val fechaCreacion: Long = 0L
) {
    fun toDomain() = Client(
        id = id,
        name = if (tipoCliente == "empresa") nombreEmpresa.ifBlank { nombre } else nombre,
        lastName = apellido,
        phone = telefono,
        email = email,
        address = direccion,
        notes = notas,
        clientType = if (tipoCliente == "empresa") "Empresa" else "Persona Natural",
        createdAt = fechaCreacion
    )
}

fun Client.toRemoto(uid: String): ClienteRemoto {
    val esEmpresa = clientType.lowercase().contains("empresa")
    return ClienteRemoto(
        id = id,
        tecnicoUid = uid,
        tipoCliente = if (esEmpresa) "empresa" else "natural",
        nombre = if (esEmpresa) "" else name,
        apellido = lastName,
        nombreEmpresa = if (esEmpresa) name else "",
        telefono = phone,
        email = email,
        direccion = address,
        notas = notes,
        fechaCreacion = createdAt
    )
}

data class ClienteResponse(
    val success: Boolean,
    val message: String? = null,
    val data: ClienteRemoto? = null
)

data class ClienteListResponse(
    val success: Boolean,
    val data: List<ClienteRemoto> = emptyList()
)
