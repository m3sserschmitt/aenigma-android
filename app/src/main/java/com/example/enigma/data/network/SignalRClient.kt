package com.example.enigma.data.network

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.enigma.crypto.AddressProvider
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.MessageSaver
import com.example.enigma.models.Neighborhood
import com.example.enigma.models.VertexBroadcastRequest
import com.example.enigma.models.hubInvocation.AuthenticationRequest
import com.example.enigma.models.hubInvocation.AuthenticateResult
import com.example.enigma.models.hubInvocation.GenerateTokenResult
import com.example.enigma.models.hubInvocation.PullResult
import com.example.enigma.models.hubInvocation.RoutingRequest
import com.example.enigma.models.hubInvocation.VertexBroadcastResult
import com.example.enigma.util.CapitalizedFieldNamingStrategy
import com.example.enigma.util.Constants.Companion.CLIENT_CONNECTION_RETRY_COUNT
import com.google.gson.GsonBuilder
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(
    private val signatureService: SignatureService,
    private val messageSaver: MessageSaver,
    private val addressProvider: AddressProvider
) {
    companion object {

        private const val ONION_ROUTING_ENDPOINT = "OnionRouting"

        private const val GENERATE_NONCE_METHOD = "GenerateToken"

        private const val AUTHENTICATE_METHOD = "Authenticate"

        private const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        private const val BROADCAST_METHOD = "Broadcast"

        private const val PULL_METHOD = "Pull"

        private const val CLEANUP_METHOD = "Cleanup"

        private const val SIGNING_DATA_ERROR = "Error while signing data."

        private const val BROADCAST_ERROR = "Broadcast failed."

        private const val INTERNAL_ERROR = "Internal error occurred."

        private const val MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
        = "The maximum number of failed connection attempts has been reached."

        private const val AUTHENTICATION_FAILED = "Authentication failed."

        private const val AUTHENTICATION_NONCE_NULL_ERROR = "Authentication nonce was null."

        private const val GENERATE_NONCE_INVOCATION_ERROR = "Error while invoking $GENERATE_NONCE_METHOD method."

        private const val AUTHENTICATE_INVOCATION_ERROR = "Error while invoking $AUTHENTICATE_METHOD method."

        private const val PULL_INVOCATION_ERROR = "Error while invoking $PULL_METHOD method."

        private const val BROADCAST_INVOCATION_ERROR = "Error while invoking $BROADCAST_METHOD method."

        private const val CONNECTION_REFUSED_ERROR = "Connection refused."

        private const val COULD_NOT_CREATE_CONNECTION_ERROR = "Could not create connection or invalid URL."

        @JvmStatic
        private fun createConnection(hostname: String): HubConnection
        {
            return HubConnectionBuilder
                .create("${hostname.trim('/', ' ')}/$ONION_ROUTING_ENDPOINT")
                .build()
        }
    }

    private lateinit var _hubConnection: HubConnection

    private lateinit var _guardAddress: String

    private var _status: MutableLiveData<SignalRStatus> = MutableLiveData(SignalRStatus.NotConnected())

    private var _failedAttempts: MutableLiveData<Int> =
        MutableLiveData(0)

    val status: LiveData<SignalRStatus> get() = _status

    val failedAttempts: LiveData<Int> get() = _failedAttempts

    private fun configureConnection()
    {
        _hubConnection.onClosed {
            updateStatus(SignalRStatus.Error.Disconnected::class.java)
        }

        _hubConnection.on(ROUTE_MESSAGE_METHOD, { data: RoutingRequest ->
            if(data.payload != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    messageSaver.handleRoutingRequest(data)
                }
            }
        }, RoutingRequest::class.java)
    }

    fun connect(hostname: String, guardAddress: String)
    {
        disconnect()
        try {
            _hubConnection = createConnection(hostname)
            this._guardAddress = guardAddress
            configureConnection()
        }
        catch (ex: Exception) {
            updateStatus(SignalRStatus.Error::class.java, COULD_NOT_CREATE_CONNECTION_ERROR)
        }
        start()
    }

    fun disconnect()
    {
        if(::_hubConnection.isInitialized) {
            try {
                _hubConnection.close()
            }
            catch (_: Exception) { }
            finally {
                _failedAttempts.postValue(0)
            }
        }
    }

    fun resetStatus()
    {
        _failedAttempts.postValue(0)
        updateStatus(SignalRStatus.NotConnected::class.java)
    }

    private fun <T: SignalRStatus> updateStatus(clazz: Class<T>, error: String? = null)
    {
        if(SignalRStatus.Error::class.java.isAssignableFrom(clazz)) {
            val newValue = _failedAttempts.value?.plus(1)
            _failedAttempts.postValue(newValue ?: 0)

            if (newValue != null) {
                if (newValue >= CLIENT_CONNECTION_RETRY_COUNT) {
                    _status.postValue(
                        SignalRStatus.Error.Aborted(
                            MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
                        )
                    )
                    return
                }
            }
        }

        when (clazz) {
            SignalRStatus.Error.ConnectionRefused::class.java -> _status.postValue(SignalRStatus.Error.ConnectionRefused(error))
            SignalRStatus.Error::class.java -> _status.postValue(SignalRStatus.Error(error))
            SignalRStatus.NotConnected::class.java -> _status.postValue(SignalRStatus.NotConnected())
            SignalRStatus.Connecting::class.java -> _status.postValue(SignalRStatus.Connecting())
            SignalRStatus.Connected::class.java -> _status.postValue(SignalRStatus.Connected())
            SignalRStatus.Authenticating::class.java -> _status.postValue(SignalRStatus.Authenticating())
            SignalRStatus.Authenticated::class.java -> {
                _status.postValue(SignalRStatus.Authenticated())
                _failedAttempts.postValue(0)
            }
            SignalRStatus.Error.Disconnected::class.java -> _status.postValue(SignalRStatus.Error.Disconnected(error))
        }
    }

    fun isConnected(): Boolean {
        if(::_hubConnection.isInitialized)
        {
            return _hubConnection.connectionState == HubConnectionState.CONNECTED
        }

        return false
    }

    private fun start()
    {
        updateStatus(SignalRStatus.Connecting::class.java)
        _hubConnection.start().subscribe(connectionsEstablishedObserver)
    }

    private fun generateNonce() {
        updateStatus(SignalRStatus.Authenticating::class.java)
        _hubConnection.invoke(GenerateTokenResult::class.java, GENERATE_NONCE_METHOD).subscribe(nonceObserver)
    }

    private fun authenticate(publicKey: String, signedData: String)
    {
        _hubConnection.invoke(AuthenticateResult::class.java, AUTHENTICATE_METHOD, AuthenticationRequest(publicKey, signedData))
            .subscribe(authenticationResultObserver)
    }

    private fun broadcast() {
        try {
            val neighborhood = Neighborhood(addressProvider.address, null, listOf(_guardAddress))
            val serializedNeighborhood = GsonBuilder()
                .setFieldNamingStrategy(CapitalizedFieldNamingStrategy())
                .create()
                .toJson(neighborhood)
            val signature = signatureService.sign(serializedNeighborhood.toByteArray())

            if (signature == null) {
                updateStatus(SignalRStatus.Error::class.java, SIGNING_DATA_ERROR)
                return
            }

            val broadcastRequest = VertexBroadcastRequest(signature.first, signature.second)
            _hubConnection.invoke(VertexBroadcastResult::class.java, BROADCAST_METHOD, broadcastRequest)
                .subscribe(broadcastResultObserver)
        } catch (_: Exception) {
            updateStatus(SignalRStatus.Error::class.java, INTERNAL_ERROR)
        }
    }

    fun pull()
    {
        _hubConnection.invoke(PullResult::class.java, PULL_METHOD).subscribe(pullResultObserver)
    }

    private fun cleanup()
    {
        _hubConnection.invoke(CLEANUP_METHOD)
    }

    private val connectionsEstablishedObserver: CompletableObserver = object: CompletableObserver
    {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable)
        {
            subscription = d
        }

        override fun onComplete() {
            updateStatus(SignalRStatus.Connected::class.java)
            generateNonce()
            subscription?.dispose()
        }

        override fun onError(e: Throwable) {
            updateStatus(SignalRStatus.Error.ConnectionRefused::class.java, CONNECTION_REFUSED_ERROR)
            subscription?.dispose()
        }
    }

    private val nonceObserver = object: SingleObserver<GenerateTokenResult> {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable)
        {
            subscription = d
        }

        override fun onError(e: Throwable)
        {
            updateStatus(SignalRStatus.Error::class.java, GENERATE_NONCE_INVOCATION_ERROR)
            subscription?.dispose()
        }

        override fun onSuccess(result: GenerateTokenResult) {
            if (result.success != true || result.data == null) {
                updateStatus(SignalRStatus.Error::class.java, AUTHENTICATION_NONCE_NULL_ERROR)
                subscription?.dispose()
                return
            }

            try {
                val decodedToken = Base64.decode(result.data, Base64.DEFAULT)
                val signature = if (decodedToken != null) signatureService.sign(decodedToken) else null

                if (signature == null) {
                    updateStatus(SignalRStatus.Error::class.java, INTERNAL_ERROR)
                    subscription?.dispose()
                    return
                }

                authenticate(signature.first, signature.second)
            } catch (_: Exception) {
                updateStatus(SignalRStatus.Error::class.java, INTERNAL_ERROR)
            }
            subscription?.dispose()
        }
    }

    private val authenticationResultObserver = object: SingleObserver<AuthenticateResult>
    {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable)
        {
            subscription = d
        }

        override fun onError(e: Throwable)
        {
            updateStatus(SignalRStatus.Error::class.java, AUTHENTICATE_INVOCATION_ERROR)
            subscription?.dispose()
        }

        override fun onSuccess(result: AuthenticateResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error::class.java, AUTHENTICATION_FAILED)
                subscription?.dispose()
                return
            }

            updateStatus(SignalRStatus.Authenticated::class.java)
            pull()
            broadcast()
            subscription?.dispose()
        }
    }

    private val broadcastResultObserver = object: SingleObserver<VertexBroadcastResult>
    {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable)
        {
            subscription = d
        }

        override fun onError(e: Throwable)
        {
            // updateStatus(SignalRStatus.Error::class.java, BROADCAST_INVOCATION_ERROR)
            subscription?.dispose()
        }

        override fun onSuccess(result: VertexBroadcastResult) {
            if (result.success != true) {
                updateStatus(SignalRStatus.Error::class.java, BROADCAST_ERROR)
            }
            subscription?.dispose()
        }
    }

    private val pullResultObserver = object: SingleObserver<PullResult>
    {
        private var subscription: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            subscription = d
        }

        override fun onError(e: Throwable)
        {
            // updateStatus(SignalRStatus.Error::class.java, PULL_INVOCATION_ERROR)
            subscription?.dispose()
        }

        override fun onSuccess(result: PullResult) {
            if(result.success == true && result.data != null)
            {
                CoroutineScope(Dispatchers.IO).launch {
                    messageSaver.handlePendingMessages(result.data)
                }
                cleanup()
            }
            subscription?.dispose()
        }
    }

    fun sendMessage(message: String): Boolean
    {
        if(!::_hubConnection.isInitialized)
        {
            return false
        }

        try {
            _hubConnection.invoke(ROUTE_MESSAGE_METHOD, RoutingRequest(message)).blockingAwait()
        } catch (ex: Exception)
        {
            return false
        }

        return true
    }
}
