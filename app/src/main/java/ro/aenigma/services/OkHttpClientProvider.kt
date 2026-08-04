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

import kotlinx.coroutines.flow.firstOrNull
import okhttp3.OkHttpClient
import ro.aenigma.data.LocalDataSource
import ro.aenigma.util.Constants
import ro.aenigma.util.Constants.Companion.OK_HTTP_CONNECT_MILLISECONDS_TIMEOUT
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class OkHttpClientProvider @Inject constructor(
    private val localDataSource: LocalDataSource,
) : IOkHttpClientProvider {
    companion object {
        @JvmStatic
        fun getInstance(
            useTor: Boolean,
            useOrbot: Boolean,
            authToken: String? = null,
            readTimeoutMilliseconds: Long = 0,
            writeTimeoutMilliseconds: Long = 0
        ): OkHttpClient {
            return OkHttpClient.Builder()
                .readTimeout(readTimeoutMilliseconds, TimeUnit.MILLISECONDS)
                .connectTimeout(OK_HTTP_CONNECT_MILLISECONDS_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(writeTimeoutMilliseconds, TimeUnit.MILLISECONDS)
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

    override suspend fun getInstance(
        readTimeoutMilliseconds: Long,
        writeTimeoutMilliseconds: Long
    ): OkHttpClient? {
        return try {
            val useTor = localDataSource.useTor.firstOrNull() == true
            val useOrbot = localDataSource.useOrbot.firstOrNull() == true
            getInstance(
                useTor = useTor,
                useOrbot = useOrbot,
                readTimeoutMilliseconds = readTimeoutMilliseconds,
                writeTimeoutMilliseconds = writeTimeoutMilliseconds
            )
        } catch (_: Exception) {
            null
        }
    }
}
