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

package ro.aenigma.models.extensions

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.rx3.await
import ro.aenigma.models.HubConnectionDto
import ro.aenigma.models.hubInvocation.AuthenticateResult
import ro.aenigma.models.hubInvocation.AuthenticationRequest
import ro.aenigma.models.hubInvocation.CleanupResult
import ro.aenigma.models.hubInvocation.GenerateTokenResult
import ro.aenigma.models.hubInvocation.PullRequest
import ro.aenigma.models.hubInvocation.PullResult
import ro.aenigma.models.hubInvocation.RouteResult
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.Constants.Companion.AUTHENTICATE_METHOD
import ro.aenigma.util.Constants.Companion.CLEANUP_METHOD
import ro.aenigma.util.Constants.Companion.CLIENT_METHOD_INVOCATION_TIMEOUT
import ro.aenigma.util.Constants.Companion.GENERATE_NONCE_METHOD
import ro.aenigma.util.Constants.Companion.PULL_METHOD
import ro.aenigma.util.Constants.Companion.ROUTE_MESSAGE_METHOD

object HubConnectionDtoExtensions {
    @JvmStatic
    fun HubConnectionDto.isClosedByClient(): Boolean {
        return closedByClient.value
    }

    @JvmStatic
    suspend fun HubConnectionDto.stop() {
        closedByClient.value = true
        return connection?.stop()?.await() ?: Unit
    }

    suspend fun <T : Any> Single<T>.awaitOrDefault(
        timeout: Long = CLIENT_METHOD_INVOCATION_TIMEOUT,
        unit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.MILLISECONDS,
        default: T?
    ): T? = try {
        timeout(timeout, unit).await()
    } catch (_: Exception) {
        default
    }

    suspend fun Completable.awaitOrDefault(
        timeout: Long = CLIENT_METHOD_INVOCATION_TIMEOUT,
        unit: java.util.concurrent.TimeUnit = java.util.concurrent.TimeUnit.MILLISECONDS
    ): Boolean = try {
        timeout(timeout, unit).await()
        true
    } catch (_: Exception) {
        false
    }

    @JvmStatic
    suspend fun HubConnectionDto.routeMessages(messages: List<String>): RouteResult? {
        return connection?.invoke(
            RouteResult::class.java, ROUTE_MESSAGE_METHOD,
            RoutingRequest(messages)
        )?.awaitOrDefault(default = null)
    }

    @JvmStatic
    suspend fun HubConnectionDto.cleanup(): CleanupResult? {
        return connection?.invoke(CleanupResult::class.java, CLEANUP_METHOD)
            ?.awaitOrDefault(default = null)
    }

    @JvmStatic
    suspend fun HubConnectionDto.pull(infId: Long?): PullResult? {
        return connection?.invoke(PullResult::class.java, PULL_METHOD, PullRequest(infId))
            ?.awaitOrDefault(default = null)
    }

    @JvmStatic
    suspend fun HubConnectionDto.authenticate(
        publicKey: String,
        signedData: String
    ): AuthenticateResult? {
        return connection?.invoke(
            AuthenticateResult::class.java, AUTHENTICATE_METHOD,
            AuthenticationRequest(publicKey, signedData)
        )?.awaitOrDefault(default = null)
    }

    @JvmStatic
    suspend fun HubConnectionDto.generateNonce(): GenerateTokenResult? {
        return connection?.invoke(GenerateTokenResult::class.java, GENERATE_NONCE_METHOD)
            ?.awaitOrDefault(default = null)
    }

    @JvmStatic
    suspend fun HubConnectionDto.start(): Boolean {
        return connection?.start()?.awaitOrDefault() ?: false
    }

    @JvmStatic
    fun HubConnectionDto.onRouteMessage(action: (RoutingRequest) -> Unit) {
        connection?.on(ROUTE_MESSAGE_METHOD, { data ->
            if (!data.payloads.isNullOrEmpty()) {
                action(data)
            }
        }, RoutingRequest::class.java)
    }

    @JvmStatic
    fun HubConnectionDto.onClosed(action: (Exception) -> Unit) {
        connection?.onClosed { e ->
            if (!isClosedByClient()) {
                action(e)
            }
        }
    }
}
