package ro.aenigma.models

data class AttachmentDto(
    val messageId: Long,
    val path: String?,
    val url: String?,
    val passphrase: String?
)
