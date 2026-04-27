package ro.aenigma.services

import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import ro.aenigma.data.LocalDataSource
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.OK_HTTP_CLIENT_TIMEOUT
import ro.aenigma.util.Constants.Companion.TOR_PROXY_HOSTNAME
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class OkHttpClientProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
) : IOkHttpClientProvider {
    companion object {
        @JvmStatic
        fun getInstance(useTor: Boolean, useOrbot: Boolean, authToken: String? = null): OkHttpClient {
            return OkHttpClient.Builder()
                .readTimeout(OK_HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
                .connectTimeout(OK_HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(OK_HTTP_CLIENT_TIMEOUT, TimeUnit.SECONDS)
                .apply {
                    if (useTor || useOrbot) {
                        proxy(
                            Proxy(
                                Proxy.Type.SOCKS,
                                InetSocketAddress(TOR_PROXY_HOSTNAME, Constants.TOR_SOCKS5_PROXY_PORT)
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
            val useOrbot = localDataSource.useOrbot.firstOrNull() == true
            getInstance(useTor, useOrbot)
        } catch (_: Exception) {
            OkHttpClientProviderDefault().getInstance()
        }
    }
}
