package com.example.enigma.data.network

import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.MessageSaver
import com.example.enigma.models.hubInvocation.AuthenticationRequest
import com.example.enigma.models.hubInvocation.AuthenticateResult
import com.example.enigma.models.hubInvocation.GenerateTokenResult
import com.example.enigma.models.hubInvocation.RoutingRequest
import com.example.enigma.util.Constants.Companion.CLIENT_CONNECTION_RETRY_COUNT
import com.google.gson.internal.LinkedTreeMap
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(
    private val signatureService: SignatureService,
    private val messageSaver: MessageSaver
) {
    companion object {

        private const val ONION_ROUTING_ENDPOINT = "OnionRouting"

        private const val GENERATE_TOKEN_METHOD = "GenerateToken"

        private const val AUTHENTICATION_METHOD = "Authenticate"

        private const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        private const val MESSAGES_SYNCHRONIZATION_METHOD = "Synchronize"

        private const val MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
        = "The maximum number of failed connection attempts has been reached."

        private const val SIGNATURE_VERIFICATION_FAILED_ERROR
        = "Signature verification failed."

        private const val AUTHENTICATION_TOKEN_NULL_ERROR
        = "Authentication token was null."

        private const val TOKEN_SIGNATURE_FAILED_ERROR
        = "Token signature failed."

        private const val CONNECTION_REFUSED_ERROR
        = "Connection refused."

        private const val COULD_NOT_CREATE_CONNECTION_ERROR
        = "Could not create connection or invalid URL."
    }

    private lateinit var hubConnection: HubConnection

    private fun configureConnection(connection: HubConnection)
    {
        connection.onClosed {
            updateStatus(SignalRStatus.Disconnected::class.java)
        }

        connection.on(ROUTE_MESSAGE_METHOD, { data: RoutingRequest ->
            if(data.payload != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    messageSaver.handleIncomingMessages(listOf(data.payload))
                }
            }
        }, RoutingRequest::class.java)

        connection.on(MESSAGES_SYNCHRONIZATION_METHOD, { data: List<LinkedTreeMap<String, String>> ->
            CoroutineScope(Dispatchers.IO).launch {
                val messages = data.mapNotNull { item -> item["content"] }
                messageSaver.handleIncomingMessages(messages)
            }
        }, List::class.java)
    }

    fun createConnection(hostname: String)
    {
        try {
            closeConnection()
            hubConnection = HubConnectionBuilder
                .create("${hostname.trim()}/$ONION_ROUTING_ENDPOINT")
                .build()

            configureConnection(hubConnection)
        }
        catch (ex: Exception) {
            updateStatus(SignalRStatus.Error::class.java, COULD_NOT_CREATE_CONNECTION_ERROR)
        }

        start()
    }

    fun closeConnection()
    {
        if(::hubConnection.isInitialized) {
            try {
                hubConnection.close()
            }
            catch (_: Exception) { }
            finally {
                _consecutiveFailedAttempts.postValue(0)
            }
        }
    }

    private var _status: MutableLiveData<SignalRStatus> =
        MutableLiveData(SignalRStatus.NotConnected())

    private var _consecutiveFailedAttempts: MutableLiveData<Int> =
        MutableLiveData(0)

    val status: LiveData<SignalRStatus> get() = _status

    val consecutiveFailedAttempts: LiveData<Int> get() = _consecutiveFailedAttempts

    fun resetStatus()
    {
        _consecutiveFailedAttempts.postValue(0)
        updateStatus(SignalRStatus.NotConnected::class.java)
    }

    private fun <T: SignalRStatus> updateStatus(clazz: Class<T>, error: String? = null)
    {
        if(SignalRStatus.Error::class.java.isAssignableFrom(clazz))
        {
            val newValue = _consecutiveFailedAttempts.value?.plus(1)
            _consecutiveFailedAttempts.postValue(newValue ?: 0)

            if (newValue != null) {
                if(newValue >= CLIENT_CONNECTION_RETRY_COUNT) {
                    _status.postValue(
                        _status.value?.let {  previousStatus ->
                            SignalRStatus.Aborted(
                                previousStatus,
                                MAXIMUM_NUMBER_OF_CONNECTION_ATTEMPTS_REACHED_ERROR
                            )
                        }
                    )
                    return
                }
            }
        }

        when (clazz) {
            SignalRStatus.Error.ConnectionRefused::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Error.ConnectionRefused(it, error) })

            SignalRStatus.Error::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Error(it, error) })

            SignalRStatus.NotConnected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.NotConnected() })

            SignalRStatus.Connecting::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Connecting(it) })

            SignalRStatus.Connected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Connected(it) })

            SignalRStatus.Authenticating::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Authenticating(it) })

            SignalRStatus.Authenticated::class.java -> {
                _status.postValue(_status.value?.let { SignalRStatus.Authenticated(it) })
                _consecutiveFailedAttempts.postValue(0)
            }

            SignalRStatus.Disconnected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Disconnected(it, error) })
        }
    }

    fun isConnected(): Boolean {
        if(::hubConnection.isInitialized)
        {
            return hubConnection.connectionState == HubConnectionState.CONNECTED
        }

        return false
    }

    private fun authenticate(): Boolean {
        updateStatus(SignalRStatus.Authenticating::class.java)
        hubConnection.invoke(GenerateTokenResult::class.java, GENERATE_TOKEN_METHOD).blockingSubscribe { generateTokenResult ->
            if(generateTokenResult != null && generateTokenResult.success == true) {
                val decodedToken = Base64.decode(generateTokenResult.data, Base64.DEFAULT)
                val signature = if(decodedToken != null ) signatureService.sign(decodedToken) else null

                if(signature != null) {
                    hubConnection.invoke(
                        AuthenticateResult::class.java,
                        AUTHENTICATION_METHOD,
                        AuthenticationRequest(
                            signature.first,
                            signature.second,
                            syncMessagesOnSuccess = true
                        )
                    ).blockingSubscribe { authenticateResultResult ->
                        if (authenticateResultResult != null && authenticateResultResult.success == true) {
                            updateStatus(SignalRStatus.Authenticated::class.java)
                        } else {
                            updateStatus(
                                SignalRStatus.Error::class.java,
                                SIGNATURE_VERIFICATION_FAILED_ERROR
                            )
                        }
                    }
                }
                else
                {
                    updateStatus(SignalRStatus.Error::class.java, TOKEN_SIGNATURE_FAILED_ERROR)
                }
            } else {
                updateStatus(SignalRStatus.Error::class.java, AUTHENTICATION_TOKEN_NULL_ERROR)
            }
        }

        return true
    }

    private fun start(authenticateOnSuccess: Boolean = true)
    {
        updateStatus(SignalRStatus.Connecting::class.java)
        hubConnection.start().blockingSubscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onComplete() {
                updateStatus(SignalRStatus.Connected::class.java)

                if(authenticateOnSuccess)
                {
                    authenticate()
                }
            }

            override fun onError(e: Throwable) {
                updateStatus(
                    SignalRStatus.Error.ConnectionRefused::class.java,
                    CONNECTION_REFUSED_ERROR
                )
            }
        })
    }

    fun sendMessage(message: String): Boolean
    {
        if(!::hubConnection.isInitialized)
        {
            return false
        }

        try {
            hubConnection.invoke(ROUTE_MESSAGE_METHOD, RoutingRequest(message)).blockingAwait()
        } catch (ex: Exception)
        {
            return false
        }

        return true
    }
}
