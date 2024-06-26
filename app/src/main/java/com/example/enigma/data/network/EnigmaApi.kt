package com.example.enigma.data.network

import com.example.enigma.models.CreatedSharedData
import com.example.enigma.models.ServerInfo
import com.example.enigma.models.SharedData
import com.example.enigma.models.SharedDataCreate
import com.example.enigma.models.Vertex
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EnigmaApi {

    @GET("/ServerInfo")
    suspend fun getServerInfo(): Response<ServerInfo?>

    @GET("/NetworkGraph")
    suspend fun getNetworkGraph(): Response<List<Vertex>?>

    @GET("/Share")
    suspend fun getSharedData(@Query("Tag") tag: String): Response<SharedData?>

    @POST("/Share")
    suspend fun createSharedData(@Body sharedDataCreate: SharedDataCreate): Response<CreatedSharedData?>
}