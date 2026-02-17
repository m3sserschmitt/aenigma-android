package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.AttachmentEntity
import ro.aenigma.models.AttachmentDto

object AttachmentEntityExtensions {
    @JvmStatic
    fun AttachmentEntity.toDto(): AttachmentDto {
        return AttachmentDto(
            messageId = messageId,
            path = path,
            url = url,
            passphrase = passphrase
        )
    }
}
