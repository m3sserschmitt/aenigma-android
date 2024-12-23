package ro.aenigma.util

import ro.aenigma.data.database.MessageEntity

class MessagesDiffUtil(
    oldList: List<MessageEntity>,
    newList: List<MessageEntity>
) : BaseDiffUtil<MessageEntity>(oldList, newList)
