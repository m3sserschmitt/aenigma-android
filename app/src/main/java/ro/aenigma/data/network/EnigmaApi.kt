package ro.aenigma.data.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import ro.aenigma.models.Article

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

    @Multipart
    @POST("/File")
    suspend fun postFile(
        @Part file: MultipartBody.Part,
        @Part("maxAccessCount") maxAccessCount: RequestBody
    ): Response<CreatedSharedData?>

    @Streaming
    @GET("/File")
    suspend fun getFile(
        @Query("tag") tag: String
    ): Response<ResponseBody>

    @GET("/{indexFile}")
    suspend fun getArticlesIndex(@Path("indexFile") fileName: String): Response<List<Article>>
}
