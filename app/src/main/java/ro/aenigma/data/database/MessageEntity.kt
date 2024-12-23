package ro.aenigma.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants
import java.time.ZonedDateTime

@Entity(tableName = Constants.MESSAGES_TABLE)
data class MessageEntity (
    val chatId: String,
    val text: String,
    val incoming: Boolean,
    var sent: Boolean,
    val date: ZonedDateTime = ZonedDateTime.now(),
    @ColumnInfo(index = true) val uuid: String? = null,
    val dateReceivedOnServer: ZonedDateTime? = null
) {
    @PrimaryKey
    @ColumnInfo(index = true)
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
