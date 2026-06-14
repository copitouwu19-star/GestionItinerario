package com.gestion.itinerario.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface CitaApiService {

    @GET("citas.php")
    suspend fun getAll(@Query("tecnico_uid") tecnicoUid: String): Response<CitaListResponse>

    @GET("citas.php")
    suspend fun getById(@Query("id") id: String): Response<CitaResponse>

    @POST("citas.php")
    suspend fun save(@Body cita: CitaRemota): Response<CitaResponse>

    @PUT("citas.php")
    suspend fun update(@Query("id") id: String, @Body cita: CitaRemota): Response<CitaResponse>

    @DELETE("citas.php")
    suspend fun delete(@Query("id") id: String): Response<CitaResponse>
}
