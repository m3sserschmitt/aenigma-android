/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2023-2026 Romulus-Emanuel Ruja <romulus.ruja@aenigma.ro>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.services

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ro.aenigma.data.LocalDataSource
import ro.aenigma.data.network.AenigmaApi
import ro.aenigma.data.network.AenigmaArticlesApi
import ro.aenigma.util.Constants.Companion.OK_HTTP_READ_MILLISECONDS_TIMEOUT
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
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

    private suspend fun getAenigmRetrofitInstance(): Retrofit? {
        return try {
            val baseUrl = localDataSource.getAppBaseApi() ?: return null
            getInstance(baseUrl, okHttpClientProvider.getInstance() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun getAenigmaArticlesRetrofitInstance(): Retrofit? {
        val baseUrl = localDataSource.getArticlesBaseApi() ?: return null
        return getInstance(baseUrl, okHttpClientProvider.getInstance() ?: return null)
    }

    suspend fun getAenigmaApi(): AenigmaApi? {
        return getAenigmRetrofitInstance()?.create(AenigmaApi::class.java)
    }

    suspend fun getAenigmaApi(baseUrl: String): AenigmaApi? {
        return getInstance(baseUrl, okHttpClientProvider.getInstance() ?: return null).create(
            AenigmaApi::class.java
        )
    }

    suspend fun getAenigmaArticlesApi(): AenigmaArticlesApi? {
        return getAenigmaArticlesRetrofitInstance()?.create(AenigmaArticlesApi::class.java)
    }

    suspend fun getAenigmaArticlesApi(baseUrl: String): AenigmaArticlesApi? {
        return getInstance(
            baseUrl,
            okHttpClientProvider.getInstance(readTimeoutMilliseconds = OK_HTTP_READ_MILLISECONDS_TIMEOUT)
                ?: return null
        ).create(AenigmaArticlesApi::class.java)
    }
}
