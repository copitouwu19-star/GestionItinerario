package com.gestion.itinerario.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface MantenimientoApiService {

    @GET("mantenimientos.php")
    suspend fun getAll(@Query("tecnico_uid") tecnicoUid: String): Response<MantenimientoListResponse>

    @GET("mantenimientos.php")
    suspend fun getById(@Query("id") id: String): Response<MantenimientoResponse>

    @POST("mantenimientos.php")
    suspend fun save(@Body m: MantenimientoRemoto): Response<MantenimientoResponse>

    @PUT("mantenimientos.php")
    suspend fun update(@Query("id") id: String, @Body m: MantenimientoRemoto): Response<MantenimientoResponse>

    @DELETE("mantenimientos.php")
    suspend fun delete(@Query("id") id: String): Response<MantenimientoResponse>
}
