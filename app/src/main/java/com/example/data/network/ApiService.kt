package com.example.data.network

import com.google.gson.JsonElement
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @GET("v3/b/{binId}/latest")
    suspend fun getBinData(
        @Path("binId") binId: String,
        @Header("X-Master-Key") masterKey: String
    ): Response<JsonElement>

    @Headers("Content-Type: application/json")
    @PUT("v3/b/{binId}")
    suspend fun updateBinData(
        @Path("binId") binId: String,
        @Header("X-Master-Key") masterKey: String,
        @Body body: Any
    ): Response<JsonElement>
}
