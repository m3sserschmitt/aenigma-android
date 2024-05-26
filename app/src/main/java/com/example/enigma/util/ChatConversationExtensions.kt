package com.example.enigma.util

import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE

fun List<MessageEntity>.isFullPage(): Boolean
{
    return this.size == CONVERSATION_PAGE_SIZE
}
