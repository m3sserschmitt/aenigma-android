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
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url
import ro.aenigma.models.Article
import ro.aenigma.models.TorCheck
import ro.aenigma.util.Constants.Companion.FILE_API_PATH
import ro.aenigma.util.Constants.Companion.SERVER_INFO_API_PATH
import ro.aenigma.util.Constants.Companion.SHARE_API_PATH
import ro.aenigma.util.Constants.Companion.VERTEX_API_PATH
import ro.aenigma.util.Constants.Companion.VERTICES_API_PATH

interface EnigmaApi {
    @GET(SERVER_INFO_API_PATH)
    suspend fun getServerInfo(): Response<ServerInfo?>

    @GET(VERTICES_API_PATH)
    suspend fun getVertices(): Response<List<Vertex>?>

    @GET(SHARE_API_PATH)
    suspend fun getSharedData(@Query("Tag") tag: String): Response<SharedData?>

    @POST(SHARE_API_PATH)
    suspend fun createSharedData(@Body sharedDataCreate: SharedDataCreate): Response<CreatedSharedData?>

    @GET(VERTEX_API_PATH)
    suspend fun getVertex(@Query("Address") address: String): Response<Vertex?>

    @Multipart
    @POST(FILE_API_PATH)
    suspend fun postFile(
        @Part file: MultipartBody.Part,
        @Part("maxAccessCount") maxAccessCount: RequestBody
    ): Response<CreatedSharedData?>

    @Streaming
    @GET(FILE_API_PATH)
    suspend fun getFile(
        @Query("tag") tag: String
    ): Response<ResponseBody>

    @GET
    suspend fun getArticlesIndex(@Url url: String): Response<List<Article>?>

    @GET
    suspend fun getStringContent(@Url url: String): Response<String?>

    @GET
    suspend fun checkTor(@Url url: String): Response<TorCheck?>
}
