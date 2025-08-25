package ro.aenigma.data.database.factories

import ro.aenigma.data.database.AttachmentEntity

object AttachmentEntityFactory {
    fun create(id: Long, path: String?, url: String?, passphrase: String?): AttachmentEntity {
        return AttachmentEntity(
            messageId = id,
            path = path,
            url = url,
            passphrase = passphrase
        )
    }
}
