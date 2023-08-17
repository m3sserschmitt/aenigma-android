package com.example.enigma.util

import com.example.enigma.data.database.MessageEntity

class MessagesDiffUtil(
    oldList: List<MessageEntity>,
    newList: List<MessageEntity>
) : BaseDiffUtil<MessageEntity>(oldList, newList)
