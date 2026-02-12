package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.models.ContactWithLastMessageDto

object ContactWithLastMessageEntityExtensions {
    @JvmStatic
    fun ContactWithLastMessage.toDto(): ContactWithLastMessageDto {
        return ContactWithLastMessageDto(
            contact = contact.toDto(),
            lastMessage = lastMessage?.toDto()
        )
    }
}
