package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.models.MessageWithDetailsDto

object MessageWithDetailsEntityExtensions {
    @JvmStatic
    fun MessageWithDetails.toDto(): MessageWithDetailsDto {
        return MessageWithDetailsDto(
            message = message.toDto(),
            sender = sender?.toDto(),
            actionFor = actionFor?.toDto()
        )
    }
}
