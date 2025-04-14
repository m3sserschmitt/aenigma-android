package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ro.aenigma.models.enums.ContactType
import ro.aenigma.util.Constants.Companion.CONTACTS_TABLE
import java.time.ZonedDateTime

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    @PrimaryKey val address: String,
    val name: String?,
    val publicKey: String?,
    val guardHostname: String?,
    val guardAddress: String?,
    val lastMessageId: Long?,
    val hasNewMessage: Boolean,
    val type: ContactType,
    val dateCreated: ZonedDateTime,
    val dateUpdated: ZonedDateTime
)
