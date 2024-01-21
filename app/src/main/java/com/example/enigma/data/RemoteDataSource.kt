package com.example.enigma.data

import com.example.enigma.data.network.EnigmaApi
import com.example.enigma.models.ServerInfo
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

    suspend fun getNetworkGraph(): Response<List<Vertex>?>
    {
        return enigmaApi.getNetworkGraph()
    }
}
