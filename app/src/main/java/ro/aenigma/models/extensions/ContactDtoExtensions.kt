package ro.aenigma.models.extensions

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ExportedContactDataDto
import java.time.ZonedDateTime

object ContactDtoExtensions {
    @JvmStatic
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

    @JvmStatic
    fun ContactDto.toExportedContactDataDto(): ExportedContactDataDto {
        return ExportedContactDataDto(
            name = name,
            publicKey = publicKey,
            guardAddress = guardAddress,
            guardHostname = guardHostname,
            address = address
        )
    }

    @JvmStatic
    fun ContactDto.withLastMessageId(lastMessageId: Long?): ContactDto {
        return copy(lastMessageId = lastMessageId)
    }

    @JvmStatic
    fun ContactDto.withName(name: String?): ContactDto {
        return copy(name = name, dateUpdated = ZonedDateTime.now())
    }

    @JvmStatic
    fun ContactDto.withNewMessage(): ContactDto {
        return copy(hasNewMessage = true)
    }

    @JvmStatic
    fun ContactDto.withGuardAddress(guardAddress: String?): ContactDto {
        return copy(guardAddress = guardAddress, dateUpdated = ZonedDateTime.now())
    }

    @JvmStatic
    fun ContactDto.withGuardHostname(guardHostname: String?): ContactDto {
        return copy(guardHostname = guardHostname, dateUpdated = ZonedDateTime.now())
    }
}
