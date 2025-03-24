package ro.aenigma.data.database.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import ro.aenigma.util.SerializerExtensions.deepCopy

object MessageEntityExtensions {
    @JvmStatic
    fun MessageEntity.getMessageTextByAction(context: Context): String
    {
        return when (this.type) {
            MessageType.DELETE -> context.getString(R.string.message_deleted)
            MessageType.DELETE_ALL -> context.getString(R.string.conversation_deleted)
            MessageType.GROUP_CREATE -> context.getString(R.string.group_created)
            MessageType.GROUP_MEMBER_ADD -> context.getString(R.string.group_member_added)
            MessageType.GROUP_MEMBER_REMOVE -> context.getString(R.string.group_member_removed)
            MessageType.GROUP_MEMBER_LEFT -> context.getString(R.string.group_member_left)
            MessageType.GROUP_RENAMED -> context.getString(R.string.group_renamed)
            MessageType.TEXT, MessageType.REPLY, null -> this.text.toString()
        }
    }

    @JvmStatic
    fun MessageEntity?.withSenderAddress(senderAddress: String?): MessageEntity? {
        return this.deepCopy()?.copy(senderAddress = senderAddress)
    }

    @JvmStatic
    fun MessageEntity?.markAsSent(): MessageEntity? {
        return this.deepCopy()?.copy(sent = true)
    }

    @JvmStatic
    fun List<MessageWithDetails>.isFullPage(): Boolean
    {
        return this.size == CONVERSATION_PAGE_SIZE
    }
}
