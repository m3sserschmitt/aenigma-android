package ro.aenigma.models.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType

object MessageDtoExtensions {
    fun MessageDto.toEntity(): MessageEntity {
        return MessageEntity(
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

    fun MessageDto.getMessageTextByAction(context: Context): String {
        return when (type) {
            MessageType.DELETE -> context.getString(R.string.message_deleted)
            MessageType.DELETE_ALL -> context.getString(R.string.conversation_deleted)
            MessageType.GROUP_CREATE -> context.getString(R.string.created_channel)
            MessageType.GROUP_MEMBER_ADD -> context.getString(R.string.added_channel_members)
            MessageType.GROUP_MEMBER_REMOVE -> context.getString(R.string.removed_channel_members)
            MessageType.GROUP_MEMBER_LEAVE -> context.getString(R.string.channel_member_left)
            MessageType.GROUP_RENAMED -> context.getString(R.string.channel_renamed)
            MessageType.FILES -> if (text.isNullOrBlank()) context.getString(R.string.files) else text
            MessageType.TEXT, MessageType.REPLY, null -> text.toString()
        }
    }

    fun MessageDto.attachmentsNotAvailable(): Boolean {
        return incoming && type == MessageType.FILES && files.isNullOrEmpty()
    }

    fun MessageDto.isNotSent(): Boolean {
        return !incoming && !sent
    }
}
