package com.example.enigma.data.network

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.Message
import com.example.enigma.util.Constants.Companion.ONION_ROUTING_ENDPOINT
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(
    private val repository: Repository,
    private val onionParsingService: OnionParsingService,
    private val signatureService: SignatureService
) {

    companion object {

        private const val GENERATE_TOKEN_ENDPOINT = "GenerateToken"

        private const val AUTHENTICATION_ENDPOINT = "Authenticate"

        private const val ROUTE_MESSAGE_ENDPOINT = "RouteMessage"

        private const val MESSAGES_SYNCHRONIZATION_ENDPOINT = "Synchronize"
    }

    private val hubConnection: HubConnection = HubConnectionBuilder
        .create(ONION_ROUTING_ENDPOINT)
        .build()

    private var authenticated = false

    init {

        hubConnection.on(GENERATE_TOKEN_ENDPOINT,
        { token ->
            if(token != null)
            {
                CoroutineScope(Dispatchers.IO).launch {
                    signatureService.sign(token).collect {
                        hubConnection.invoke(AUTHENTICATION_ENDPOINT, it.first, it.second)
                    }
                }
            }
        }, String::class.java)

        hubConnection.on(AUTHENTICATION_ENDPOINT, { result: Boolean ->
            authenticated = result
        }, Boolean::class.java)

        hubConnection.onClosed {
            authenticated = false
        }

        hubConnection.on(ROUTE_MESSAGE_ENDPOINT, { data: String ->
            handleIncomingMessages(listOf(data))
        }, String::class.java)

        hubConnection.on(MESSAGES_SYNCHRONIZATION_ENDPOINT, { data: List<String> ->
            handleIncomingMessages(data)
        }, List::class.java)
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
        return hubConnection.connectionState == HubConnectionState.CONNECTED
    }

    fun isAuthenticated(): Boolean {
        return authenticated
    }

    fun authenticate() {
        hubConnection.invoke(GENERATE_TOKEN_ENDPOINT).blockingAwait()
    }

    fun start() {
        hubConnection.start().blockingAwait()
    }

    fun sendMessage(message: String)
    {
        hubConnection.invoke(ROUTE_MESSAGE_ENDPOINT, message).blockingAwait(5, TimeUnit.SECONDS)
    }
}
