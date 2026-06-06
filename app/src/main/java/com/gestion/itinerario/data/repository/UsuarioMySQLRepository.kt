package com.gestion.itinerario.data.repository

import com.gestion.itinerario.data.remote.UsuarioApiService
import com.gestion.itinerario.data.remote.UsuarioListResponse
import com.gestion.itinerario.data.remote.UsuarioRemoto
import javax.inject.Inject

class UsuarioMySQLRepository @Inject constructor(
    private val api: UsuarioApiService
) {
    /**
     * Registra o actualiza el usuario en MySQL justo después de
     * un login/registro exitoso en Firebase.
     * Usa INSERT … ON DUPLICATE KEY UPDATE del lado del servidor,
     * por lo que es seguro llamarlo tanto en registro como en login.
     */
    suspend fun sincronizarUsuario(
        firebaseUid: String,
        nombre: String,
        apellido: String = "",
        email: String,
        telefono: String = "",
        empresa: String = ""
    ): Result<UsuarioRemoto> = runCatching {
        val body = UsuarioRemoto(
            firebaseUid = firebaseUid,
            nombre = nombre,
            apellido = apellido.ifBlank { null },
            email = email,
            telefono = telefono.ifBlank { null },
            empresa = empresa.ifBlank { null }
        )
        val response = api.guardarUsuario(body)
        response.body()?.data
            ?: error(response.body()?.message ?: "Sin respuesta del servidor")
    }

    suspend fun getUsuarios(): Result<List<UsuarioRemoto>> = runCatching {
        val response = api.getUsuarios()
        response.body()?.data ?: emptyList()
    }

    suspend fun desactivarUsuario(id: Int): Result<Unit> = runCatching {
        api.desactivarUsuario(id)
        Unit
    }
}
