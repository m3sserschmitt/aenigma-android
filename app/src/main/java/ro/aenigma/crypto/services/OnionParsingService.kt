package ro.aenigma.crypto.services

import android.content.Context
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.Constants
import ro.aenigma.util.HexConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(@ApplicationContext context: Context) {

    private var ready = false

    init {
        if (KeysManager.generateKeyIfNotExistent(context)) {
            val key = KeysManager.readPrivateKey(context)
            if (key != null) {
                ready = CryptoProvider.initDecryptionEx(key)
            }
        }
    }

    fun parse(routingRequest: RoutingRequest): List<ParsedMessageDto> {
        return routingRequest.payloads?.mapNotNull { item ->
            parse(
                PendingMessage(
                    routingRequest.uuid,
                    null,
                    item,
                    ZonedDateTime.now().toString(),
                    false
                )
            )
        } ?: listOf()
    }

    fun parse(pendingMessage: PendingMessage): ParsedMessageDto? {
        if (!ready || pendingMessage.content == null) {
            return null
        }
        synchronized(this)
        {
            return try {
                val decryptedData =
                    CryptoProvider.unsealOnionEx(pendingMessage.content) ?: return null
                if (decryptedData.size < Constants.ADDRESS_SIZE + 1) {
                    return null
                }
                val address =
                    HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE))
                val content =
                    String(decryptedData.sliceArray(Constants.ADDRESS_SIZE until decryptedData.size))
                val dateReceivedOnServer = ZonedDateTime.parse(pendingMessage.dateReceived)
                    .withZoneSameInstant(ZoneId.systemDefault())
                ParsedMessageDto(address, content, dateReceivedOnServer, pendingMessage.uuid)
            } catch (_: Exception) {
                null
            }
        }
    }
}
