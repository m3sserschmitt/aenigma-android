package ro.aenigma.models

import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime

data class ContactDto (
    val address: String,
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
