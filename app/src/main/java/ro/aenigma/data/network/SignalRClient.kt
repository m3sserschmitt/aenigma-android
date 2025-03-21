package ro.aenigma.data.network

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.services.MessageSaver
import ro.aenigma.models.Neighborhood
import ro.aenigma.models.VertexBroadcastRequest
import ro.aenigma.models.hubInvocation.AuthenticateResult
import ro.aenigma.models.hubInvocation.AuthenticationRequest
import ro.aenigma.models.hubInvocation.CleanupResult
import ro.aenigma.models.hubInvocation.GenerateTokenResult
import ro.aenigma.models.hubInvocation.PullResult
import ro.aenigma.models.hubInvocation.RouteResult
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.models.hubInvocation.VertexBroadcastResult
import ro.aenigma.util.CapitalizedFieldNamingStrategy
import ro.aenigma.util.toJson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(
    private val signatureService: SignatureService,
    private val messageSaver: MessageSaver
) {
    companion object {
        const val CLIENT_CONNECTION_RETRY_COUNT = 3

        private const val ONION_ROUTING_ENDPOINT = "OnionRouting"

        private const val GENERATE_NONCE_METHOD = "GenerateToken"

        private const val AUTHENTICATE_METHOD = "Authenticate"

        private const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        private const val BROADCAST_METHOD = "Broadcast"

        private const val PULL_METHOD = "Pull"

        private const val CLEANUP_METHOD = "Cleanup"

        private const val INTERNAL_ERROR = "Internal error occurred."

        private const val MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR =
            "The maximum number of failed connection attempts has been reached."

        private const val AUTHENTICATION_NONCE_NULL_ERROR = "Authentication nonce was null."

        private const val PULL_DATA_NULL_ERROR =
            "Data returned from $PULL_METHOD invocation was null."

        private const val GENERATE_NONCE_INVOCATION_ERROR =
            "Error while invoking $GENERATE_NONCE_METHOD method."

        private const val COULD_NOT_CREATE_CONNECTION_ERROR =
            "Could not create connection or invalid URL."

        @JvmStatic
        private fun createConnection(hostname: String): HubConnection {
            return HubConnectionBuilder
                .create("${hostname.trim('/', ' ')}/$ONION_ROUTING_ENDPOINT")
                .build()
        }
    }

    private lateinit var _hubConnection: HubConnection

    private lateinit var _guardAddress: String

    private var _status: MutableLiveData<SignalRStatus> =
        MutableLiveData(SignalRStatus.NotConnected())

    private var _failedAttempts: MutableLiveData<Int> =
        MutableLiveData(0)

    val status: LiveData<SignalRStatus> get() = _status

    val failedAttempts: LiveData<Int> get() = _failedAttempts

    private fun configureConnection() {
        synchronized(_hubConnection) {
            _hubConnection.onClosed {
                updateStatus(SignalRStatus.Error.Disconnected(_status.value))
            }
        }
        synchronized(_hubConnection) {
            _hubConnection.on(ROUTE_MESSAGE_METHOD, { data: RoutingRequest ->
                if (data.payloads != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        messageSaver.handleRoutingRequest(data)
                    }
                }
            }, RoutingRequest::class.java)
        }
    }

    fun connect(hostname: String, guardAddress: String) {
        if (isConnected()) {
            return
        }
        try {
            _hubConnection = createConnection(hostname)
            this._guardAddress = guardAddress
            configureConnection()
        } catch (ex: Exception) {
            updateStatus(SignalRStatus.Error(_status.value!!, COULD_NOT_CREATE_CONNECTION_ERROR))
        }
        return start()
    }

    fun disconnect() {
        if (!isConnected()) {
            return
        }
        if (::_hubConnection.isInitialized) {
            try {
                synchronized(_hubConnection)
                {
                    return _hubConnection.close()
                }
            } catch (_: Exception) {
            } finally {
                _failedAttempts.postValue(0)
            }
        }
    }

    fun resetAborted() {
        if (status.value is SignalRStatus.Error.Aborted) {
            _failedAttempts.postValue(0)
            updateStatus(SignalRStatus.Reset(_status.value))
        }
    }

    private fun updateStatus(status: SignalRStatus) {
        if (status is SignalRStatus.Error) {
            val newValue = _failedAttempts.value?.plus(1)
            _failedAttempts.postValue(newValue ?: 0)

            if (newValue != null && newValue >= CLIENT_CONNECTION_RETRY_COUNT) {
                _status.postValue(
                    SignalRStatus.Error.Aborted(
                        MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
                    )
                )
                return
            }
        }
        _status.postValue(status)
    }

    fun isConnected(): Boolean {
        if (::_hubConnection.isInitialized) {
            synchronized(_hubConnection) {
                return _hubConnection.connectionState == HubConnectionState.CONNECTED
            }
        }

        return false
    }

    private fun start() {
        if (!isConnected()) {
            updateStatus(SignalRStatus.Connecting())
            synchronized(_hubConnection)
            {
                return _hubConnection.start().blockingSubscribe(connectionsEstablishedObserver)
            }
        }
    }

    private fun generateNonce() {
        if (isConnected()) {
            updateStatus(SignalRStatus.Authenticating())
            synchronized(_hubConnection) {
                return _hubConnection.invoke(GenerateTokenResult::class.java, GENERATE_NONCE_METHOD)
                    .blockingSubscribe(nonceObserver)
            }
        }
    }

    private fun authenticate(publicKey: String, signedData: String) {
        if (isConnected()) {
            synchronized(_hubConnection) {
                return _hubConnection.invoke(
                    AuthenticateResult::class.java, AUTHENTICATE_METHOD,
                    AuthenticationRequest(publicKey, signedData)
                ).blockingSubscribe(authenticationResultObserver)
            }
        }
    }

    fun broadcast() {
        if (!isConnected()) {
            return
        }
        try {
            updateStatus(SignalRStatus.Broadcasting())
            val neighborhood =
                Neighborhood(signatureService.address, null, listOf(_guardAddress))
            val serializedNeighborhood = neighborhood.toJson()

            if(serializedNeighborhood == null)
            {
                updateStatus(SignalRStatus.Error(_status.value))
                return
            }

            val signature = signatureService.sign(serializedNeighborhood.toByteArray())

            if (signature == null) {
                updateStatus(SignalRStatus.Error(_status.value))
                return
            }

            val broadcastRequest = VertexBroadcastRequest(signature.first, signature.second)
            synchronized(_hubConnection) {
                return _hubConnection.invoke(
                    VertexBroadcastResult::class.java, BROADCAST_METHOD,
                    broadcastRequest
                ).blockingSubscribe(broadcastResultObserver)
            }
        } catch (ex: Exception) {
            updateStatus(SignalRStatus.Error(_status.value, ex.message))
        }
    }

    fun pull() {
        if (isConnected()) {
            updateStatus(SignalRStatus.Pulling())
            synchronized(_hubConnection) {
                return _hubConnection.invoke(PullResult::class.java, PULL_METHOD)
                    .blockingSubscribe(pullResultObserver)
            }
        }
    }

    fun cleanup() {
        if (isConnected()) {
            updateStatus(SignalRStatus.Cleaning())
            synchronized(_hubConnection) {
                return _hubConnection.invoke(CleanupResult::class.java, CLEANUP_METHOD)
                    .blockingSubscribe(cleanupResultObserver)
            }
        }
    }

    private val connectionsEstablishedObserver: CompletableObserver = object : CompletableObserver {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onComplete() {
            updateStatus(SignalRStatus.Connected())
            generateNonce()
            subscription?.dispose()
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error.ConnectionRefused(e.message))
            subscription?.dispose()
        }
    }

    private val nonceObserver = object : SingleObserver<GenerateTokenResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error(_status.value, e.message))
            subscription?.dispose()
        }

        override fun onSuccess(result: GenerateTokenResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            } else if (result.data == null) {
                updateStatus(SignalRStatus.Error(_status.value, AUTHENTICATION_NONCE_NULL_ERROR))
            } else {
                try {
                    val decodedToken = Base64.decode(result.data, Base64.DEFAULT)
                    val signature =
                        if (decodedToken != null) signatureService.sign(decodedToken) else null

                    if (signature != null) {
                        authenticate(signature.first, signature.second)
                    } else {
                        updateStatus(SignalRStatus.Error(_status.value, INTERNAL_ERROR))
                    }
                } catch (ex: Exception) {
                    updateStatus(SignalRStatus.Error(_status.value, ex.message))
                }
            }
            subscription?.dispose()
        }
    }

    private val authenticationResultObserver = object : SingleObserver<AuthenticateResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error(_status.value, e.message))
            subscription?.dispose()
        }

        override fun onSuccess(result: AuthenticateResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
                subscription?.dispose()
            } else {
                updateStatus(SignalRStatus.Authenticated())
            }
            subscription?.dispose()
        }
    }

    private val broadcastResultObserver = object : SingleObserver<VertexBroadcastResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error(_status.value, e.message))
            subscription?.dispose()
        }

        override fun onSuccess(result: VertexBroadcastResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            } else {
                updateStatus(SignalRStatus.Broadcasted())
            }
            subscription?.dispose()
        }
    }

    private val pullResultObserver = object : SingleObserver<PullResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error(_status.value, e.message))
            subscription?.dispose()
        }

        override fun onSuccess(result: PullResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            } else if (result.data == null) {
                updateStatus(SignalRStatus.Error(_status.value, PULL_DATA_NULL_ERROR))
            } else {
                updateStatus(SignalRStatus.Synchronized())
                CoroutineScope(Dispatchers.IO).launch {
                    messageSaver.handlePendingMessages(result.data)
                }
            }
            subscription?.dispose()
        }
    }

    private val cleanupResultObserver = object : SingleObserver<CleanupResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error(_status.value, e.message))
            subscription?.dispose()
        }

        override fun onSuccess(result: CleanupResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error(_status.value, result.errorsToString()))
            } else {
                updateStatus(SignalRStatus.Clean())
            }
            subscription?.dispose()
        }
    }

    fun sendMessages(messages: List<String>): Boolean
    {
        var r = false
        if (isConnected()) {
            synchronized(_hubConnection) {
                _hubConnection.invoke(
                    RouteResult::class.java, ROUTE_MESSAGE_METHOD,
                    RoutingRequest(messages)
                ).blockingSubscribe(
                    { result -> r = result.success == true },
                    { _ -> r = false }
                )
            }
        }
        return r
    }
}
