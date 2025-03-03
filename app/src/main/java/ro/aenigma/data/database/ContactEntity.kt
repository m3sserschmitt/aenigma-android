package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ro.aenigma.models.enums.ContactType
import ro.aenigma.util.Constants.Companion.CONTACTS_TABLE
import java.time.ZonedDateTime

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    @PrimaryKey val address: String,
    var name: String,
    var publicKey: String,
    var guardHostname: String?,
    var guardAddress: String,
    val type: ContactType,
    var hasNewMessage: Boolean,
    var lastSynchronized: ZonedDateTime
) {
    var lastMessageId: Long? = null

    fun toContactWithPreview(): ContactWithConversationPreview
    {
        return ContactWithConversationPreview(
            address = address,
            name = name,
            publicKey = publicKey,
            guardHostname = guardHostname,
            guardAddress = guardAddress,
            hasNewMessage = hasNewMessage,
            lastSynchronized = lastSynchronized,
            lastMessageId = lastMessageId,
            type = type
        )
    }
}
