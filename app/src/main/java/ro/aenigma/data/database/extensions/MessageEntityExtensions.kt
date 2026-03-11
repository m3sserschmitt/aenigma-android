package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE

object MessageEntityExtensions {
    @JvmStatic
    fun List<MessageWithDetailsDto>.isFullPage(): Boolean {
        return size == CONVERSATION_PAGE_SIZE
    }

    @JvmStatic
    fun MessageEntity.toDto(): MessageDto {
        return MessageDto(
            id = id,
            chatId = chatId,
            senderAddress = senderAddress,
            serverUUID = serverUUID,
            text = text,
            type = type,
            actionFor = actionFor,
            refId = refId,
            incoming = incoming,
            sent = sent,
            deleted = deleted,
            date = date,
            dateReceivedOnServer = dateReceivedOnServer,
            files = files
        )
    }
}
