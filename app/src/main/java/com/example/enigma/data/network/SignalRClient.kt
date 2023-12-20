package com.example.enigma.data.network

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.AuthenticationRequest
import com.example.enigma.models.Message
import com.example.enigma.models.PendingMessage
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
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val signatureService: SignatureService
) {
    companion object {

        private const val ONION_ROUTING_ENDPOINT = "OnionRouting"

        private const val GENERATE_TOKEN_METHOD = "GenerateToken"

        private const val AUTHENTICATION_METHOD = "Authenticate"

        private const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        private const val MESSAGES_SYNCHRONIZATION_METHOD = "Synchronize"
    }

    private lateinit var hubConnection: HubConnection

    private fun configureConnection(connection: HubConnection)
    {
        connection.onClosed {
            updateStatus(SignalRStatus.Disconnected::class.java)
        }

        connection.on(ROUTE_MESSAGE_METHOD, { data: String ->
            handleIncomingMessages(listOf(data))
        }, String::class.java)

        connection.on(MESSAGES_SYNCHRONIZATION_METHOD, { data: List<PendingMessage> ->
            handleIncomingMessages(data.map { it.content })
        }, List::class.java)
    }

    fun createConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.local.getGuard().collect { guard ->
                try {
                    hubConnection = HubConnectionBuilder
                        .create("${guard.hostname.trim()}/$ONION_ROUTING_ENDPOINT")
                        .build()

                    configureConnection(hubConnection)
                }
                catch (ex: Exception) {
                    updateStatus(SignalRStatus.Error::class.java,
                        "Could not create connection or invalid URL.")
                }

                start(true)
            }
        }
    }

    private var _status: MutableLiveData<SignalRStatus> =
        MutableLiveData(SignalRStatus.NotConnected())

    val status get() = _status

    private fun <T: SignalRStatus> updateStatus(clazz: Class<T>, error: String? = null)
    {
        when (clazz) {

            SignalRStatus.Error::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Error(it, error) })

            SignalRStatus.NotConnected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.NotConnected() })

            SignalRStatus.Connecting::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Connecting(it, error) })

            SignalRStatus.Connected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Connected(it, error) })

            SignalRStatus.Authenticating::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Authenticating(it, error) })

            SignalRStatus.Authenticated::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Authenticated(it, error) })

            SignalRStatus.Disconnected::class.java ->
                _status.postValue(_status.value?.let { SignalRStatus.Disconnected(it, error) })
        }
    }

    private fun handleIncomingMessages(messages: List<String>)
    {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        for (ciphertext in messages)
        {
            scope.launch {
                onionParsingService.parse(ciphertext).collect {
                    saveMessage(it)
                }
            }
        }
    }

    private suspend fun saveMessage(message: Message)
    {
        repository.local.insertMessage(
            MessageEntity(message.chatId, message.text, true, message.date)
        )

        repository.local.markConversationAsUnread(message.chatId)
    }

    fun isConnected(): Boolean {
        if(::hubConnection.isInitialized)
        {
            return hubConnection.connectionState == HubConnectionState.CONNECTED
        }

        return false
    }

    @SuppressLint("CheckResult")
    private fun authenticate(): Boolean {

        updateStatus(SignalRStatus.Authenticating::class.java)

        hubConnection.invoke(String::class.java, GENERATE_TOKEN_METHOD).subscribe { token: String? ->

            if(token != null) {
                CoroutineScope(Dispatchers.IO).launch {

                    signatureService.sign(token).collect {

                        if(it.second != null) {

                            hubConnection.invoke(
                                Boolean::class.java,
                                AUTHENTICATION_METHOD,
                                AuthenticationRequest(
                                    it.first,
                                    it.second!!,
                                    syncMessagesOnSuccess = true,
                                    updateNetworkGraph = false
                                )
                            ).subscribe { authenticated ->
                                if(authenticated) {
                                    updateStatus(SignalRStatus.Authenticated::class.java)
                                } else {
                                    updateStatus(SignalRStatus.Error::class.java,
                                        "Signature verification failed")
                                }
                            }
                        } else {
                            updateStatus(SignalRStatus.Error::class.java, "Signing token failed")
                        }
                    }
                }
            } else {
                updateStatus(SignalRStatus.Error::class.java, "Authentication token was null")
            }
        }

        return true
    }

    private fun start(authenticateOnSuccess: Boolean)
    {
        updateStatus(SignalRStatus.Connecting::class.java)

        hubConnection.start().subscribe(object : CompletableObserver {
            override fun onSubscribe(d: Disposable) { }

            override fun onComplete() {
                updateStatus(SignalRStatus.Connected::class.java)

                if(authenticateOnSuccess)
                {
                    authenticate()
                }
            }

            override fun onError(e: Throwable) {
                updateStatus(SignalRStatus.Error::class.java, "Connection refused")
            }
        })
    }

    fun sendMessage(message: String): Boolean
    {
        if(!::hubConnection.isInitialized)
        {
            return false
        }

        hubConnection.invoke(ROUTE_MESSAGE_METHOD, message)

        return true
    }
}
