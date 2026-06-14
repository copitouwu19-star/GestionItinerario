package com.gestion.itinerario.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ClientApiService {

    @GET("clientes.php")
    suspend fun getAll(@Query("tecnico_uid") tecnicoUid: String): Response<ClienteListResponse>

    @GET("clientes.php")
    suspend fun getById(@Query("id") id: String): Response<ClienteResponse>

    @POST("clientes.php")
    suspend fun save(@Body cliente: ClienteRemoto): Response<ClienteResponse>

    @PUT("clientes.php")
    suspend fun update(@Query("id") id: String, @Body cliente: ClienteRemoto): Response<ClienteResponse>

    @DELETE("clientes.php")
    suspend fun delete(@Query("id") id: String): Response<ClienteResponse>
}
