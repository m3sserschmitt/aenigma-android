package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.models.ContactDto

object ContactEntityExtensions {
    @JvmStatic
    fun ContactEntity.toDto(): ContactDto {
        return ContactDto(
            address = address,
            name = name,
            publicKey = publicKey,
            guardHostname = guardHostname,
            guardAddress = guardAddress,
            lastMessageId = lastMessageId,
            hasNewMessage = hasNewMessage,
            type = type,
            dateCreated = dateCreated,
            dateUpdated = dateUpdated
        )
    }
}
