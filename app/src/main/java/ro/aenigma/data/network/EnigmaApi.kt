package ro.aenigma.data.network

import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface EnigmaApi {
    @GET("/Info")
    suspend fun getServerInfo(): Response<ServerInfo?>

    @GET("/Vertices")
    suspend fun getVertices(): Response<List<Vertex>?>

    @GET("/Share")
    suspend fun getSharedData(@Query("Tag") tag: String): Response<SharedData?>

    @POST("/Share")
    suspend fun createSharedData(@Body sharedDataCreate: SharedDataCreate): Response<CreatedSharedData?>

    @GET("Vertex")
    suspend fun getVertex(@Query("Address") address: String): Response<Vertex?>
}
