package ro.aenigma.data.database

import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime

data class ContactWithConversationPreview (
    val address: String,
    var name: String,
    val publicKey: String,
    val guardHostname: String?,
    val guardAddress: String,
    val type: ContactType,
    val hasNewMessage: Boolean,
    val lastSynchronized: ZonedDateTime,
    val lastMessageId: Long? = null,
    val lastMessageText: String? = null,
    val lastMessageIncoming: Boolean? = null
) {
    fun toContact(): ContactEntity {
        val contact = ContactEntity(
            address,
            name,
            publicKey,
            guardHostname,
            guardAddress,
            type,
            hasNewMessage,
            lastSynchronized
        )
        contact.lastMessageId = lastMessageId
        return contact
    }
}
