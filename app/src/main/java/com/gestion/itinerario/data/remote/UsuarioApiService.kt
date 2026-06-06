package com.gestion.itinerario.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface UsuarioApiService {

    @GET("usuarios.php")
    suspend fun getUsuarios(): Response<UsuarioListResponse>

    @GET("usuarios.php")
    suspend fun getUsuarioByFirebaseUid(
        @Query("firebase_uid") firebaseUid: String
    ): Response<UsuarioResponse>

    @POST("usuarios.php")
    suspend fun guardarUsuario(@Body usuario: UsuarioRemoto): Response<UsuarioResponse>

    @PUT("usuarios.php")
    suspend fun actualizarUsuario(
        @Query("id") id: Int,
        @Body usuario: UsuarioRemoto
    ): Response<UsuarioResponse>

    @DELETE("usuarios.php")
    suspend fun desactivarUsuario(@Query("id") id: Int): Response<UsuarioResponse>
}
