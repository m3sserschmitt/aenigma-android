package ro.aenigma.crypto.services

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ro.aenigma.models.ParsedMessageDto
import ro.aenigma.models.PendingMessageDto
import ro.aenigma.models.hubInvocation.RoutingRequest
import ro.aenigma.util.Constants
import ro.aenigma.util.HexConverter
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.KeysManager
import ro.aenigma.util.ZonedDateTimeExtensions.normalize
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnionParsingService @Inject constructor(keysManager: KeysManager) {

    private val mutex = Mutex()

    private var ready = false

    init {
        val key = keysManager.readPrivateKey()
        ready = key != null && CryptoProvider.initDecryptionEx(key)
    }

    suspend fun parse(routingRequest: RoutingRequest): List<ParsedMessageDto> {
        return routingRequest.payloads?.mapNotNull { item ->
            parse(
                PendingMessageDto(
                    id = null,
                    uuid = routingRequest.uuid,
                    destination = null,
                    content = item,
                    dateReceived = ZonedDateTime.now().toString(),
                    sent = false
                )
            )
        } ?: listOf()
    }

    suspend fun parse(pendingMessageDto: PendingMessageDto): ParsedMessageDto? {
        if (!ready || pendingMessageDto.content == null) {
            return null
        }
        mutex.withLock {
            return try {
                val decryptedData =
                    CryptoProvider.unsealOnionEx(pendingMessageDto.content) ?: return null
                if (decryptedData.size < Constants.ADDRESS_SIZE_BYTES + 1) {
                    return null
                }
                val chatId =
                    HexConverter.toHex(decryptedData.sliceArray(0 until Constants.ADDRESS_SIZE_BYTES))
                val content =
                    String(decryptedData.sliceArray(Constants.ADDRESS_SIZE_BYTES until decryptedData.size))
                val dateReceivedOnServer = pendingMessageDto.dateReceived?.normalize()
                ParsedMessageDto(chatId, content, dateReceivedOnServer, pendingMessageDto.uuid)
            } catch (_: Exception) {
                null
            }
        }
    }
}
