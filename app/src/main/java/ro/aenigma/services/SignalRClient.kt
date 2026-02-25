package ro.aenigma.services

import android.util.Base64
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.data.Repository
import ro.aenigma.models.HubConnectionDto
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.authenticate
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.cleanup
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.stop
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.generateNonce
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.onClosed
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.onRouteMessage
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.pull
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.routeMessages
import ro.aenigma.models.extensions.HubConnectionDtoExtensions.start
import ro.aenigma.models.hubInvocation.AuthenticateResult
import ro.aenigma.models.hubInvocation.CleanupResult
import ro.aenigma.models.hubInvocation.GenerateTokenResult
import ro.aenigma.models.hubInvocation.PullResult
import ro.aenigma.util.Constants.Companion.ONION_ROUTING_ENDPOINT
import ro.aenigma.util.Constants.Companion.PULL_METHOD
import ro.aenigma.util.Constants.Companion.SEND_MESSAGES_CHUNK_SIZE
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_PORT
import ro.aenigma.util.Constants.Companion.SOCKS5_PROXY_HOSTNAME
import ro.aenigma.util.StringExtensions.getHttpUri
import java.net.InetSocketAddress
import java.net.Proxy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(
    private val repository: Repository,
    private val signatureService: SignatureService,
    private val messageSaver: MessageSaver
) {
    companion object {
        const val CLIENT_CONNECTION_RETRY_COUNT = 3

        private const val INTERNAL_ERROR = "Internal error occurred."

        private const val MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR =
            "The maximum number of failed connection attempts has been reached."

        private const val AUTHENTICATION_NONCE_NULL_ERROR = "Authentication nonce was null."

        private const val PULL_DATA_NULL_ERROR =
            "Data returned from $PULL_METHOD invocation was null."

        private const val COULD_NOT_CREATE_CONNECTION_ERROR =
            "Could not create connection or invalid URL."

        @JvmStatic
        fun createConnection(
            useTor: Boolean,
            useOrbot: Boolean,
            hostname: String
        ): HubConnectionDto? {
            val uri = hostname.getHttpUri(ONION_ROUTING_ENDPOINT) ?: return null
            return HubConnectionDto(
                connection = HubConnectionBuilder
                    .create(uri)
                    .apply {
                        if (useTor || useOrbot) {
                            withTransport(TransportEnum.LONG_POLLING)
                        }
                        if (useTor) {
                            setHttpClientBuilderCallback { builder ->
                                builder.proxy(
                                    Proxy(
                                        Proxy.Type.SOCKS,
                                        InetSocketAddress(SOCKS5_PROXY_HOSTNAME, SOCKS5_PROXY_PORT)
                                    )
                                )
                            }
                        }
                    }.build()
            )
        }
    }

    private val _routingScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _hubConnection = MutableStateFlow(HubConnectionDto())

    private val _status = MutableStateFlow<SignalRStatus>(SignalRStatus.NotConnected)

    private val _failedAttempts = MutableStateFlow(0)

    val status: StateFlow<SignalRStatus> = _status

    private fun configureConnection(dto: HubConnectionDto) {
        dto.onClosed { updateStatus(SignalRStatus.Error.Disconnected()) }
        dto.onRouteMessage { data ->
            _routingScope.launch {
                messageSaver.handleRoutingRequest(data)
            }
        }
    }

    suspend fun connect(hostname: String): Boolean {
        if (!isConnected()) {
            try {
                val useTor = repository.local.useTor.firstOrNull() == true
                val useOrbot = repository.local.useOrbot.firstOrNull() == true
                createConnection(useTor, useOrbot, hostname)?.let { dto ->
                    configureConnection(dto)
                    _hubConnection.value = dto
                    return start()
                }
                return false
            } catch (_: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, COULD_NOT_CREATE_CONNECTION_ERROR))
                return false
            }
        } else {
            return false
        }
    }

    suspend fun disconnect(): Boolean {
        if (isConnected()) {
            try {
                _hubConnection.value.stop()
                _failedAttempts.value = 0
                updateStatus(SignalRStatus.Error.Disconnected())
                return true
            } catch (_: Exception) {
                return false
            }
        } else {
            return false
        }
    }

    fun resetAborted() {
        if (status.value is SignalRStatus.Error.Aborted) {
            _failedAttempts.value = 0
            updateStatus(SignalRStatus.NotConnected)
        }
    }

    private fun updateStatus(status: SignalRStatus) {
        if (status is SignalRStatus.Error) {
            val newValue = _failedAttempts.value.plus(1)
            _failedAttempts.value = newValue

            if (newValue >= CLIENT_CONNECTION_RETRY_COUNT) {
                _status.value = SignalRStatus.Error.Aborted(
                    MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
                )
                return
            }
        }
        _status.value = status
    }

    fun isConnected(): Boolean {
        return _status.value greaterOrEqualThan SignalRStatus.Connected
    }

    fun nonceGenerated(): Boolean {
        return _status.value greaterOrEqualThan SignalRStatus.Authenticating
    }

    fun isAuthenticated(): Boolean {
        return _status.value greaterOrEqualThan SignalRStatus.Authenticated
    }

    private suspend fun start(): Boolean {
        if (!isConnected()) {
            updateStatus(SignalRStatus.Connecting)
            try {
                _hubConnection.value.start()
                return generateNonce()
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error.ConnectionRefused(e.message))
                return false
            }
        } else {
            return false
        }
    }

    private suspend fun generateNonce(): Boolean {
        if (!nonceGenerated()) {
            updateStatus(SignalRStatus.Authenticating)
            try {
                val result = _hubConnection.value.generateNonce()
                return if(result == null) {
                    false
                } else {
                    onNonceGenerated(result)
                }
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, e.message))
                return false
            }
        } else {
            return false
        }
    }

    private suspend fun authenticate(publicKey: String, signedData: String): Boolean {
        if (!isAuthenticated()) {
            try {
                val result = _hubConnection.value.authenticate(publicKey, signedData)
                return if(result == null) {
                    false
                } else {
                    onSuccessAuthentication(result)
                }
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, e.message))
                return false
            }
        } else {
            return false
        }
    }

    suspend fun pull(): Boolean {
        if (isAuthenticated()) {
            updateStatus(SignalRStatus.Pulling)
            try {
                val result = _hubConnection.value.pull()
                return if(result == null) {
                    false
                } else {
                    onSuccessPull(result)
                }
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, e.message))
                return false
            }
        } else {
            return false
        }
    }

    suspend fun cleanup(): Boolean {
        if (isAuthenticated()) {
            updateStatus(SignalRStatus.Cleaning)
            try {
                val result = _hubConnection.value.cleanup()
                return if(result == null) {
                    false
                } else {
                    onSuccessCleanup(result)
                }
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, e.message))
                return false
            }
        } else {
            return false
        }
    }

    private suspend fun onNonceGenerated(result: GenerateTokenResult): Boolean {
        if (result.success != true) {
            updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            return false
        } else if (result.data == null) {
            updateStatus(SignalRStatus.Error(_status.value, AUTHENTICATION_NONCE_NULL_ERROR))
            return false
        } else {
            try {
                val decodedToken = Base64.decode(result.data, Base64.DEFAULT)
                val signature =
                    if (decodedToken != null) signatureService.sign(decodedToken) else null

                if (signature != null && signature.publicKey != null && signature.signedData != null) {
                    return authenticate(signature.publicKey, signature.signedData)
                } else {
                    updateStatus(SignalRStatus.Error(_status.value, INTERNAL_ERROR))
                    return false
                }
            } catch (ex: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, ex.message))
                return false
            }
        }
    }

    private fun onSuccessAuthentication(result: AuthenticateResult): Boolean {
        if (result.success != true) {
            updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            return false
        } else {
            updateStatus(SignalRStatus.Authenticated)
            return true
        }
    }

    private fun onSuccessPull(result: PullResult): Boolean {
        if (result.success != true) {
            updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            return false
        } else if (result.data == null) {
            updateStatus(SignalRStatus.Error(_status.value, PULL_DATA_NULL_ERROR))
            return false
        } else {
            _routingScope.launch {
                messageSaver.handlePendingMessages(result.data)
                updateStatus(SignalRStatus.Synchronized)
            }
            return true
        }
    }

    private fun onSuccessCleanup(result: CleanupResult): Boolean {
        if (result.success != true) {
            updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            return false
        } else {
            updateStatus(SignalRStatus.Clean)
            return true
        }
    }

    private suspend fun sendMessages(messages: List<String>): Boolean {
        if (isAuthenticated()) {
            try {
                val result = _hubConnection.value.routeMessages(messages)
                return if(result == null) {
                    false
                } else {
                    result.success ?: false
                }
            } catch (e: Exception) {
                updateStatus(SignalRStatus.Error(_status.value, e.message))
                return false
            }
        } else {
            return false
        }
    }

    suspend fun sendChunkedMessages(messages: List<String>): Boolean {
        var success = true
        for(chunk in messages.chunked(SEND_MESSAGES_CHUNK_SIZE)) {
            success = success && sendMessages(chunk)
        }
        return success
    }
}
