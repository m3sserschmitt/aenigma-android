package com.example.enigma.data.network

import com.example.enigma.crypto.OnionParsingService
import com.example.enigma.crypto.SignatureService
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.models.AuthenticationRequest
import com.example.enigma.models.Message
import com.example.enigma.models.PendingMessage
import com.example.enigma.util.Constants.Companion.ONION_ROUTING_ENDPOINT
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
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

        private const val GENERATE_TOKEN_METHOD = "GenerateToken"

        private const val AUTHENTICATION_METHOD = "Authenticate"

        private const val ROUTE_MESSAGE_METHOD = "RouteMessage"

        private const val MESSAGES_SYNCHRONIZATION_METHOD = "Synchronize"
    }

    private val hubConnection: HubConnection = HubConnectionBuilder
        .create(ONION_ROUTING_ENDPOINT)
        .build()

    private var authenticated = false

    init {

        hubConnection.onClosed {
            authenticated = false
        }

        hubConnection.on(ROUTE_MESSAGE_METHOD, { data: String ->
            (listOf(data))
        }, String::class.java)

        hubConnection.on(MESSAGES_SYNCHRONIZATION_METHOD, { data: List<PendingMessage> ->
            handleIncomingMessages(data.map { it.content })
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
        val token = hubConnection.invoke(String::class.java, GENERATE_TOKEN_METHOD).blockingGet()

        CoroutineScope(Dispatchers.IO).launch {
            signatureService.sign(token).collect {
                authenticated = hubConnection.invoke(Boolean::class.java, AUTHENTICATION_METHOD,
                    AuthenticationRequest(
                        it.first,
                        it.second,
                        syncMessagesOnSuccess = true,
                        updateNetworkGraph = false
                    )
                ).blockingGet()
            }
        }
    }

    fun start() {
        hubConnection.start().blockingAwait()
    }

    fun sendMessage(message: String)
    {
        hubConnection.invoke(ROUTE_MESSAGE_METHOD, message)
    }
}
