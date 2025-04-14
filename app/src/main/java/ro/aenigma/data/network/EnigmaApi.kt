package ro.aenigma.data.network

import okhttp3.OkHttpClient
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ServerInfo
import ro.aenigma.models.SharedData
import ro.aenigma.models.SharedDataCreate
import ro.aenigma.models.Vertex
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
import ro.aenigma.util.getBaseUrl
import java.util.concurrent.TimeUnit

interface EnigmaApi {
    companion object {
        @JvmStatic
        fun initApi(url: String): EnigmaApi {
            return Retrofit.Builder()
                .baseUrl(url.getBaseUrl())
                .client(
                    OkHttpClient.Builder()
                        .readTimeout(10, TimeUnit.SECONDS)
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .build()
                )
                .addConverterFactory(createJsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(EnigmaApi::class.java)
        }
    }

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
