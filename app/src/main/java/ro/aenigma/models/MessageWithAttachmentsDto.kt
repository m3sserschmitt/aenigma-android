package ro.aenigma.models

data class MessageWithAttachmentsDto (
    val message: MessageDto,
    val attachment: AttachmentDto?
)
