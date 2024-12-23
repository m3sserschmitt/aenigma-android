package ro.aenigma.util

import ro.aenigma.data.database.MessageEntity
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE

fun List<MessageEntity>.isFullPage(): Boolean
{
    return this.size == CONVERSATION_PAGE_SIZE
}
