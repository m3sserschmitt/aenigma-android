package ro.aenigma.models.factories

import ro.aenigma.models.ContactDto
import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime

object ContactDtoFactory {
    @JvmStatic
    fun createContact(
        address: String, name: String?, publicKey: String?, guardHostname: String?,
        guardAddress: String?
    ): ContactDto {
        val dateCreated = ZonedDateTime.now()
        return ContactDto(
            address = address,
            name = name,
            publicKey = publicKey,
            guardHostname = guardHostname,
            guardAddress = guardAddress,
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastMessageId = null,
            dateCreated = dateCreated,
            dateUpdated = dateCreated
        )
    }

    @JvmStatic
    fun createGroup(address: String, name: String?): ContactDto {
        val dateCreated = ZonedDateTime.now()
        return ContactDto(
            address = address,
            name = name,
            publicKey = null,
            guardHostname = null,
            guardAddress = null,
            type = ContactType.GROUP,
            hasNewMessage = false,
            lastMessageId = null,
            dateCreated = dateCreated,
            dateUpdated = dateCreated
        )
    }
}