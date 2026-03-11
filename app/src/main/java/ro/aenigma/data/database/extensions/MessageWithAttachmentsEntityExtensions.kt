package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.MessageWithAttachments
import ro.aenigma.data.database.extensions.AttachmentEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.models.MessageWithAttachmentsDto

object MessageWithAttachmentsEntityExtensions {
    @JvmStatic
    fun MessageWithAttachments.toDto(): MessageWithAttachmentsDto {
        return MessageWithAttachmentsDto(
            message = message.toDto(),
            attachment = attachment?.toDto()
        )
    }
}
