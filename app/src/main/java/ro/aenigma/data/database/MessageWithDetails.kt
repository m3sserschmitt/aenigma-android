package ro.aenigma.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithDetails (
    @Embedded
    val message: MessageEntity,

    @Relation(
        parentColumn = "senderAddress",
        entityColumn = "address"
    )
    val sender: ContactEntity?,

    @Relation(
        parentColumn = "actionFor",
        entityColumn = "refId"
    )
    val actionFor: MessageEntity?
)
