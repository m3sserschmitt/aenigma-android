package ro.aenigma.data.database.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.models.Artifact
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import ro.aenigma.util.SerializerExtensions.deepCopy

object MessageEntityExtensions {
    @JvmStatic
    fun MessageEntity.getMessageTextByAction(context: Context): String {
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
    fun MessageEntity?.withSenderAddress(senderAddress: String?): MessageEntity? {
        return this.deepCopy()?.copy(senderAddress = senderAddress)
    }

    @JvmStatic
    fun MessageEntity?.withId(id: Long): MessageEntity? {
        return this.deepCopy()?.copy(id = id)
    }

    @JvmStatic
    fun MessageEntity?.markAsSent(): MessageEntity? {
        return this.deepCopy()?.copy(sent = true)
    }

    @JvmStatic
    fun MessageEntity?.withText(text: String?): MessageEntity? {
        return this.deepCopy()?.copy(text = text)
    }

    @JvmStatic
    fun MessageEntity?.markAsDeleted(): MessageEntity? {
        return this.deepCopy()?.copy(deleted = true)
    }

    @JvmStatic
    fun List<MessageWithDetails>.isFullPage(): Boolean {
        return this.size == CONVERSATION_PAGE_SIZE
    }

    @JvmStatic
    fun MessageEntity.toArtifact(
        senderName: String?,
        guardAddress: String?,
        guardHostname: String?,
        resourceUrl: String?,
        chatId: String?,
        passphrase: String?
    ): Artifact? {
        return Artifact(
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

    @JvmStatic
    fun MessageEntity.isText(): Boolean {
        return type == MessageType.TEXT || type == MessageType.REPLY
    }

    @JvmStatic
    fun MessageEntity.isFile(): Boolean {
        return type == MessageType.FILES
    }

    @JvmStatic
    fun MessageEntity.isGroupUpdate(): Boolean {
        return type == MessageType.GROUP_CREATE
                || type == MessageType.GROUP_RENAMED
                || type == MessageType.GROUP_MEMBER_ADD
                || type == MessageType.GROUP_MEMBER_REMOVE
    }

    @JvmStatic
    fun MessageEntity.isDelete(): Boolean {
        return type == MessageType.DELETE || type == MessageType.DELETE_ALL
    }
}
