package ro.aenigma.data

import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
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
