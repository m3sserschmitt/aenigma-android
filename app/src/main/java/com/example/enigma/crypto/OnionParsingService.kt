package com.example.enigma.crypto

import android.content.Context
import com.example.enigma.models.Message
import com.example.enigma.models.PendingMessage
import com.example.enigma.models.hubInvocation.RoutingRequest
import com.example.enigma.util.Constants
import com.example.enigma.util.HexConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(@ApplicationContext context: Context) {

    private var ready = false

    init {
        val key = KeysManager.readPrivateKey(context)
        if (key != null) {
            ready = CryptoProvider.initDecryptionEx(key)
        }
    }

    fun parse(routingRequest: RoutingRequest): Message? {
        return parse(
            PendingMessage(
                routingRequest.uuid,
                null,
                routingRequest.payload,
                ZonedDateTime.now().toString(),
                false
            )
        )
    }

    fun parse(pendingMessage: PendingMessage): Message? {
        if (!ready || pendingMessage.content == null) {
            return null
        }
        synchronized(this)
        {
            return try {
                val decryptedData =
                    CryptoProvider.unsealOnionEx(pendingMessage.content) ?: return null
                val address =
                    HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE))
                val content =
                    String(decryptedData.sliceArray(Constants.ADDRESS_SIZE until decryptedData.size))
                val dateReceivedOnServer = ZonedDateTime.parse(pendingMessage.dateReceived)
                    .withZoneSameInstant(ZoneId.systemDefault())
                Message(address, content, true, dateReceivedOnServer, pendingMessage.uuid)
            } catch (_: Exception) {
                null
            }
        }
    }
}
