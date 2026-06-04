package ro.aenigma.services

import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import ro.aenigma.data.LocalDataSource
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.OK_HTTP_CONNECT_TIMEOUT
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
                .readTimeout(0, TimeUnit.SECONDS)
                .connectTimeout(OK_HTTP_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .apply {
                    if (useTor || useOrbot) {
                        proxy(Constants.TOR_PROXY)
                        dns(Constants.TOR_DNS)
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

    override suspend fun getInstance(): OkHttpClient? {
        return try {
            val useTor = localDataSource.useTor.firstOrNull() == true
            val useOrbot = localDataSource.useOrbot.firstOrNull() == true
            getInstance(useTor, useOrbot)
        } catch (_: Exception) {
            null
        }
    }
}
