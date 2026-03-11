package ro.aenigma.models.extensions

import ro.aenigma.data.database.AttachmentEntity
import ro.aenigma.models.AttachmentDto

object AttachmentDtoExtensions {
    @JvmStatic
    fun AttachmentDto.toEntity(): AttachmentEntity {
        return AttachmentEntity(
            messageId = messageId,
            path = path,
            url = url,
            passphrase = passphrase
        )
    }
}
