package ro.aenigma.models.extensions

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.models.ContactDto

object ContactDtoExtensions {
    fun ContactDto.toEntity(): ContactEntity = ContactEntity(
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
