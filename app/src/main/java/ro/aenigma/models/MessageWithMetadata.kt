package ro.aenigma.models

data class MessageWithMetadata(
    val text: String?,
    val action: MessageActionDto?,
    val senderName: String?,
    val groupResourceUrl: String?,
    val senderGuardAddress: String?,
    val senderGuardHostname: String?,
    val senderPublicKey: String?,
    val refId: String?
)
