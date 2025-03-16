package ro.aenigma.data.database

import androidx.room.Embedded
import androidx.room.Relation

class ContactWithLastMessage(
    @Embedded
    val contact: ContactEntity,

    @Relation(
        parentColumn = "lastMessageId",
        entityColumn = "id"
    )
    val lastMessage: MessageEntity?
)
