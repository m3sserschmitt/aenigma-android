package com.example.enigma.data.network

import android.util.Base64
import com.example.enigma.crypto.CryptoContext
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.onion.OnionParser
import com.example.enigma.models.Message
import com.example.enigma.util.Constants.Companion.ONION_ROUTING_ENDPOINT
import com.example.enigma.util.Constants.Companion.PASSPHRASE
import com.example.enigma.util.Constants.Companion.PRIVATE_KEY
import com.example.enigma.util.Constants.Companion.PUBLIC_KEY
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SignalRClient @Inject constructor(private val repository: Repository) {

    companion object {

        private const val GENERATE_TOKEN_ENDPOINT = "GenerateToken"

        private const val AUTHENTICATION_ENDPOINT = "Authenticate"

        private const val ROUTE_MESSAGE_ENDPOINT = "RouteMessage"

        private const val MESSAGES_SYNCHRONIZATION_ENDPOINT = "Synchronize"
    }

    private val hubConnection: HubConnection = HubConnectionBuilder
        .create(ONION_ROUTING_ENDPOINT)
        .build()

    private val parser by lazy {
        OnionParser(CryptoContext.Factory.createDecryptionContext(PRIVATE_KEY, PASSPHRASE))
    }

    private var authenticated = false

    init {

        hubConnection.on(GENERATE_TOKEN_ENDPOINT,
        { token ->
            val decodedToken = Base64.decode(token, Base64.DEFAULT)
            val signature = CryptoProvider.sign(PRIVATE_KEY, PASSPHRASE, decodedToken)
            val encodedSignature = Base64.encodeToString(signature, Base64.DEFAULT)

            hubConnection.invoke(AUTHENTICATION_ENDPOINT, PUBLIC_KEY, encodedSignature)
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
        for (message in messages)
        {
            val decodedMessage = Base64.decode(message, Base64.DEFAULT)
            val decryptedMessage = parser.parse(decodedMessage)

            if (decryptedMessage != null) {
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

                scope.launch { saveMessage(decryptedMessage) }
            }
        }
    }

    private suspend fun emitEvent(message: Message)
    {
        val eventBus = SignalREventBus.getInstance()
        val event = MessageReceivedEvent(message)

        eventBus.invokeEvent(event)
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
