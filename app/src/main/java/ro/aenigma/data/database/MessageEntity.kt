package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.Constants
import java.time.ZonedDateTime

@Entity(
    tableName = Constants.MESSAGES_TABLE,
    indices = [
        Index(value = ["chatId", "deleted", "id"]),
        Index(value = ["refId"], unique = true),
        Index(value = ["serverUUID"], unique = true)
    ]
)
data class MessageEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val chatId: String,
    val senderAddress: String?,
    val serverUUID: String?,
    val text: String?,
    val type: MessageType?,
    val actionFor: String?,
    val refId: String?,
    val incoming: Boolean,
    val sent: Boolean,
    val deleted: Boolean,
    val date: ZonedDateTime,
    val dateReceivedOnServer: ZonedDateTime?,
    val files: List<String>?
)
