/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

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
import ro.aenigma.data.network.EnigmaApi
import ro.aenigma.util.SerializerExtensions.createJsonConverterFactory
import ro.aenigma.util.StringExtensions.getBaseUrl
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

    suspend fun getInstance(): Retrofit? {
        return try {
            val baseUrl = localDataSource.getGuardHostname()?.getBaseUrl() ?: return null
            getInstance(baseUrl, okHttpClientProvider.getInstance() ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun getApi(): EnigmaApi? {
        return getInstance()?.create(EnigmaApi::class.java)
    }

    private suspend fun getInstance(baseUrl: String): Retrofit? {
        return getInstance(baseUrl, okHttpClientProvider.getInstance() ?: return null)
    }

    suspend fun getApi(baseUrl: String): EnigmaApi? {
        return getInstance(baseUrl)?.create(EnigmaApi::class.java)
    }
}
