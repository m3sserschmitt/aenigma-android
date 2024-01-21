package com.example.enigma.data.network

import com.example.enigma.models.ServerInfo
import com.example.enigma.models.Vertex
import retrofit2.Response
import retrofit2.http.GET

interface EnigmaApi {

    @GET("/ServerInfo")
    suspend fun getServerInfo(): Response<ServerInfo?>

    @GET("/NetworkGraph")
    suspend fun getNetworkGraph(): Response<List<Vertex>?>
}