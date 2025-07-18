package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_TABLE

@Entity(
    tableName = ATTACHMENTS_TABLE,
    foreignKeys = [ForeignKey(
        entity = MessageEntity::class,
        parentColumns = ["id"],
        childColumns = ["messageId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AttachmentEntity(
    @PrimaryKey val messageId: Long,
    val path: String?,
    val url: String?,
    val passphrase: String?
)
