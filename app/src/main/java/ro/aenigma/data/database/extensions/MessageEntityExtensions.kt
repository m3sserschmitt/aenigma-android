package ro.aenigma.data.database.extensions

import android.content.Context
import ro.aenigma.R
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.models.enums.MessageActionType

object MessageEntityExtensions {
    fun MessageEntity.getMessageTextByAction(context: Context): String
    {
        return when (this.action.actionType) {
            MessageActionType.DELETE -> context.getString(R.string.message_deleted)
            MessageActionType.DELETE_ALL -> context.getString(R.string.conversation_deleted)
            MessageActionType.GROUP_CREATE -> context.getString(R.string.group_created)
            MessageActionType.GROUP_MEMBER_ADD -> context.getString(R.string.group_member_added)
            MessageActionType.GROUP_MEMBER_REMOVE -> context.getString(R.string.group_member_removed)
            MessageActionType.GROUP_MEMBER_LEFT -> context.getString(R.string.group_member_left)
            MessageActionType.GROUP_RENAMED -> context.getString(R.string.group_renamed)
            MessageActionType.TEXT, MessageActionType.REPLY -> this.text
        }
    }
}
