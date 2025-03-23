package ro.aenigma.models

data class MessageWithMetadata(
    val text: String? = null,
    val action: MessageActionDto? = null,
    val senderName: String? = null,
    val groupResourceUrl: String? = null,
    val senderGuardAddress: String? = null,
    val senderGuardHostname: String? = null,
    val senderPublicKey: String? = null,
    val refId: String? = null
)
