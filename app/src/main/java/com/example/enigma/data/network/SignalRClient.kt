package com.example.enigma.data.network

import android.util.Base64
import com.example.enigma.crypto.CryptoContext
import com.example.enigma.crypto.CryptoProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.onion.OnionParser
import com.example.enigma.models.Message
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

    private val hubConnection: HubConnection = HubConnectionBuilder
        .create("http://10.0.2.2:5000/OnionRouting")
        .build()

    private val parser by lazy {
        OnionParser(CryptoContext.Factory.createDecryptionContext(PRIVATE_KEY, PASSPHRASE))
    }

    private var authenticated = false

    init {

        hubConnection.on("GenerateToken",
        { token ->
            val decodedToken = Base64.decode(token, Base64.DEFAULT)
            val signature = CryptoProvider.sign(PRIVATE_KEY, PASSPHRASE, decodedToken)
            val encodedSignature = Base64.encodeToString(signature, Base64.DEFAULT)

            hubConnection.invoke("Authenticate", PUBLIC_KEY, encodedSignature)
        }, String::class.java)

        hubConnection.on("Authenticate", { result: Boolean ->
            authenticated = result
        }, Boolean::class.java)

        hubConnection.onClosed {
            authenticated = false
        }

        hubConnection.on("RouteMessage", { data: String ->
            val decodedMessage = Base64.decode(data, Base64.DEFAULT)
            val decryptedMessage = parser.parse(decodedMessage)

            if (decryptedMessage != null) {
                val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

                // scope.launch { emitEvent(decryptedMessage) }
                scope.launch { saveMessage(decryptedMessage) }
            }
        }, String::class.java)
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
            MessageEntity(message.chatId, message.text, true, message.date))
    }

    fun isConnected(): Boolean {
        return hubConnection.connectionState == HubConnectionState.CONNECTED
    }

    fun isAuthenticated(): Boolean {
        return authenticated
    }

    fun authenticate() {
        hubConnection.invoke("GenerateToken").blockingAwait()
    }

    fun start() {
        hubConnection.start().blockingAwait()
    }

    fun sendMessage(message: String)
    {
        hubConnection.invoke("RouteMessage", message).blockingAwait(5, TimeUnit.SECONDS)
    }
}
