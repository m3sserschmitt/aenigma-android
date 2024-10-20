package com.example.enigma.data

import com.example.enigma.data.network.EnigmaApi
import com.example.enigma.models.CreatedSharedData
import com.example.enigma.models.ServerInfo
import com.example.enigma.models.SharedData
import com.example.enigma.models.SharedDataCreate
import com.example.enigma.models.Vertex
import retrofit2.Response
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val enigmaApi: EnigmaApi
) {

    suspend fun getServerInfo(): Response<ServerInfo?>
    {
        return enigmaApi.getServerInfo()
    }

    suspend fun getVertices(): Response<List<Vertex>?>
    {
        return enigmaApi.getVertices()
    }

    suspend fun createSharedData(sharedDataCreate: SharedDataCreate): Response<CreatedSharedData?>
    {
        return enigmaApi.createSharedData(sharedDataCreate)
    }

    suspend fun getSharedData(tag: String): Response<SharedData?>
    {
        return enigmaApi.getSharedData(tag)
    }

    suspend fun getVertex(address: String): Response<Vertex?>
    {
        return enigmaApi.getVertex(address)
    }
}
