package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants
import ro.aenigma.util.MessageType
import java.time.ZonedDateTime
import java.util.UUID

@Entity(
    tableName = Constants.MESSAGES_TABLE,
    indices = [
        Index(value = ["chatId"]),
        Index(value = ["sent"]),
        Index(value = ["deleted"]),
        Index(value = ["refId"]),
        Index(value = ["uuid"], unique = true)
    ]
)
data class MessageEntity (
    val chatId: String,
    val text: String,
    val incoming: Boolean,
    val uuid: String?,
    var sent: Boolean = false,
    var deleted: Boolean = false,
    val type: MessageType = MessageType.TEXT,
    val date: ZonedDateTime = ZonedDateTime.now(),
    val dateReceivedOnServer: ZonedDateTime? = null,
    val refId: String? = UUID.randomUUID().toString(),

    ) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MessageEntity) return false
        return id == other.id
    }
}
