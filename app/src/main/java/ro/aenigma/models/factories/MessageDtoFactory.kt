package ro.aenigma.models.factories

import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime
import java.util.UUID

object MessageDtoFactory {
    @JvmStatic
    fun createIncoming(
        chatId: String, senderAddress: String?, serverUUID: String?, text: String?,
        type: MessageType?, actionFor: String?, refId: String?,
        dateReceivedOnServer: ZonedDateTime?, attachments: List<String> = listOf()
    ): MessageDto {
        return MessageDto(
            id = 0,
            chatId = chatId,
            senderAddress = senderAddress,
            serverUUID = serverUUID,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = refId,
            incoming = true,
            sent = false,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = dateReceivedOnServer,
            files = attachments
        )
    }

    @JvmStatic
    fun createOutgoing(
        chatId: String,
        text: String?,
        type: MessageType?,
        actionFor: String?,
        attachments: List<String> = listOf()
    ): MessageDto {
        return MessageDto(
            id = 0,
            chatId = chatId,
            senderAddress = null,
            serverUUID = null,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = UUID.randomUUID().toString(),
            incoming = false,
            sent = false,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = null,
            files = attachments
        )
    }
}
