package ro.aenigma.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ro.aenigma.data.LocalDataSource
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.util.Constants.Companion.API_BASE_URL
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
import ro.aenigma.util.UrlExtensions.getBaseUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
    private val okHttpClientProvider: OkHttpClientProvider
) {
    companion object {
        @JvmStatic
        private fun getInstance(baseUrl: String, client: OkHttpClient): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(createJsonConverterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }
    }

    suspend fun getInstance(): Retrofit {
        return try {
            val baseUrl = localDataSource.getGuard()?.hostname?.getBaseUrl() ?: API_BASE_URL
            getInstance(baseUrl, okHttpClientProvider.getInstance())
        } catch (_: Exception) {
            getInstance(API_BASE_URL, okHttpClientProvider.getInstance())
        }
    }

    suspend fun getApi(): EnigmaApi {
        return getInstance().create(EnigmaApi::class.java)
    }

    suspend fun getInstance(baseUrl: String): Retrofit {
        return getInstance(baseUrl, okHttpClientProvider.getInstance())
    }

    suspend fun getApi(baseUrl: String): EnigmaApi {
        return getInstance(baseUrl).create(EnigmaApi::class.java)
    }
}
