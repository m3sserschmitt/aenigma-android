package ro.aenigma.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithAttachments(
    @Embedded
    val message: MessageEntity,
    @Relation(parentColumn = "id", entityColumn = "messageId")
    val attachment: AttachmentEntity?
)
