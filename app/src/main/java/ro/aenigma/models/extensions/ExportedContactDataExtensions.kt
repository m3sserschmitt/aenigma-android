package ro.aenigma.models.extensions

import ro.aenigma.models.ContactDto
import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.enums.ContactType

object ExportedContactDataExtensions {
    fun ExportedContactDataDto.toContactDto(): ContactDto {
        return ContactDto(
            address = address ?: "",
            name = name,
            publicKey = publicKey,
            guardHostname = guardHostname,
            guardAddress = guardAddress,
            lastMessageId = null,
            hasNewMessage = false,
            type = ContactType.CONTACT,
            dateCreated = dateTimeCreated,
            dateUpdated = dateTimeCreated,
        )
    }
}
