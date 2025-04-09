package ro.aenigma.data.database.factories

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime
import java.util.UUID

object MessageEntityFactory {
    @JvmStatic
    fun createIncoming(
        chatId: String, senderAddress: String?, serverUUID: String?, text: String?,
        type: MessageType?, actionFor: String?, refId: String?,
        dateReceivedOnServer: ZonedDateTime?
    ): MessageEntity {
        return MessageEntity(
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
            dateReceivedOnServer = dateReceivedOnServer
        )
    }

    @JvmStatic
    fun createOutgoing(
        chatId: String,
        text: String?,
        type: MessageType?,
        actionFor: String?
    ): MessageEntity {
        return MessageEntity(
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
            dateReceivedOnServer = null
        )
    }
}
