package ro.aenigma.models.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.ArtifactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.MessageType

object MessageDtoExtensions {
    @JvmStatic
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

    @JvmStatic
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

    @JvmStatic
    fun MessageDto.attachmentsNotAvailable(): Boolean {
        return incoming && type == MessageType.FILES && files.isNullOrEmpty() && filesLate.value.isEmpty()
    }

    @JvmStatic
    fun MessageDto.isNotSent(): Boolean {
        return !incoming && !sent
    }

    @JvmStatic
    fun MessageDto.isText(): Boolean {
        return type == MessageType.TEXT || type == MessageType.REPLY
    }

    @JvmStatic
    fun MessageDto.isFile(): Boolean {
        return type == MessageType.FILES
    }

    @JvmStatic
    fun MessageDto.isGroupUpdate(): Boolean {
        return type == MessageType.GROUP_CREATE
                || type == MessageType.GROUP_RENAMED
                || type == MessageType.GROUP_MEMBER_ADD
                || type == MessageType.GROUP_MEMBER_REMOVE
    }

    @JvmStatic
    fun MessageDto.isDelete(): Boolean {
        return type == MessageType.DELETE || type == MessageType.DELETE_ALL
    }

    @JvmStatic
    fun MessageDto.withSenderAddress(senderAddress: String?): MessageDto {
        return copy(senderAddress = senderAddress)
    }

    @JvmStatic
    fun MessageDto.markAsSent(): MessageDto {
        return copy(sent = true)
    }

    @JvmStatic
    fun MessageDto.markAsDeleted(): MessageDto {
        return copy(deleted = true)
    }

    @JvmStatic
    fun MessageDto.toArtifactDto(
        senderName: String?,
        guardAddress: String?,
        guardHostname: String?,
        resourceUrl: String?,
        chatId: String?,
        passphrase: String?
    ): ArtifactDto {
        return ArtifactDto(
            text = text,
            type = type,
            actionFor = actionFor,
            senderName = senderName,
            guardAddress = guardAddress,
            guardHostname = guardHostname,
            refId = refId,
            resourceUrl = resourceUrl,
            senderAddress = senderAddress,
            chatId = chatId,
            passphrase = passphrase
        )
    }
}
