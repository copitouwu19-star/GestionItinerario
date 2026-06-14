package com.gestion.itinerario.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface PerfilApiService {

    @GET("perfil_empresa.php")
    suspend fun get(@Query("firebase_uid") firebaseUid: String): Response<PerfilResponse>

    @POST("perfil_empresa.php")
    suspend fun save(@Body perfil: PerfilRemoto): Response<PerfilResponse>

    @PUT("perfil_empresa.php")
    suspend fun update(
        @Query("firebase_uid") firebaseUid: String,
        @Body perfil: PerfilRemoto
    ): Response<PerfilResponse>
}
