package ro.aenigma.services

import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ro.aenigma.data.LocalDataSource
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.util.Constants.Companion.API_BASE_URL
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_PORT
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_ADDRESS
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
import ro.aenigma.util.getBaseUrl
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.Objects
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val signalrController: dagger.Lazy<SignalrConnectionController>
) {
    companion object {
        private const val CONNECTION_TIMEOUT: Long = 15

        @JvmStatic
        private fun createRetrofit(useTor: Boolean, baseUrl: String, authToken: String? = null): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(createHttpClient(useTor, authToken))
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(createJsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }

        @JvmStatic
        fun createHttpClient(useTor: Boolean, authToken: String? = null): OkHttpClient {
            return OkHttpClient.Builder()
                .readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
                .apply {
                    if (useTor) {
                        proxy(
                            Proxy(
                                Proxy.Type.SOCKS,
                                InetSocketAddress(SOCKS5_PROXY_ADDRESS, SOCKS5_PROXY_PORT)
                            )
                        )
                    }
                    if(!authToken.isNullOrBlank()) {
                        addInterceptor { chain ->
                            chain.proceed(
                                chain.request().newBuilder()
                                    .addHeader("Authorization", "Bearer $authToken")
                                    .build()
                            )
                        }
                    }
                }.build()
        }
    }

    private var instance: Retrofit? = null

    private var previousHash: Int? = null

    suspend fun getInstance(): Retrofit {
        val useTor = localDataSource.useTor.firstOrNull() == true
        val baseUrl = localDataSource.getGuard()?.hostname?.getBaseUrl() ?: API_BASE_URL
        val authToken = signalrController.get().authToken.value
        val hash = Objects.hash(useTor, baseUrl, authToken)
        if (hash != previousHash || instance == null) {
            instance = createRetrofit(useTor, baseUrl, authToken)
            previousHash = hash
        }
        return instance!!
    }

    suspend fun getApi(): EnigmaApi {
        return getInstance().create(EnigmaApi::class.java)
    }

    suspend fun getInstance(baseUrl: String): Retrofit {
        val useTor = localDataSource.useTor.firstOrNull() == true
        return createRetrofit(useTor, baseUrl)
    }

    suspend fun getApi(baseUrl: String): EnigmaApi {
        return getInstance(baseUrl).create(EnigmaApi::class.java)
    }
}
