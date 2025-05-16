package ro.aenigma.crypto.services

import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.PendingMessage
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.Constants
import ro.aenigma.util.HexConverter
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(keysManager: KeysManager) {

    private val lock = Any()

    private var ready = false

    init {
        if (keysManager.generateKeyIfNotExistent()) {
            val key = keysManager.readPrivateKey()
            ready = key != null && CryptoProvider.initDecryptionEx(key)
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
        synchronized(lock)
        {
            return try {
                val decryptedData =
                    CryptoProvider.unsealOnionEx(pendingMessage.content) ?: return null
                if (decryptedData.size < Constants.ADDRESS_SIZE_BYTES + 1) {
                    return null
                }
                val address =
                    HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE_BYTES))
                val content =
                    String(decryptedData.sliceArray(Constants.ADDRESS_SIZE_BYTES until decryptedData.size))
                val dateReceivedOnServer = ZonedDateTime.parse(pendingMessage.dateReceived)
                    .withZoneSameInstant(ZoneId.systemDefault())
                ParsedMessageDto(address, content, dateReceivedOnServer, pendingMessage.uuid)
            } catch (_: Exception) {
                null
            }
        }
    }
}
