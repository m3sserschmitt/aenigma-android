package ro.aenigma.services

import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import ro.aenigma.data.LocalDataSource
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_ADDRESS
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_PORT
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class OkHttpClientProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val signalrController: dagger.Lazy<SignalrConnectionController>
) : IOkHttpClientProvider {
    companion object {
        private const val CONNECTION_TIMEOUT: Long = 15

        @JvmStatic
        fun getInstance(useTor: Boolean, authToken: String? = null): OkHttpClient {
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
                    if (!authToken.isNullOrBlank()) {
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

    override suspend fun getInstance(): OkHttpClient {
        return try {
            val useTor = localDataSource.useTor.firstOrNull() == true
            val authToken = signalrController.get().authToken.value
            getInstance(useTor, authToken)
        } catch (_: Exception) {
            OkHttpClientProviderDefault().getInstance()
        }
    }
}
