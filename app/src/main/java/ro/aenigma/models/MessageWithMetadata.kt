package ro.aenigma.models

import ro.aenigma.models.enums.MessageType

open class MessageWithMetadata(
    val text: String? = null,
    val type: MessageType? = null,
    val senderName: String? = null,
    val groupResourceUrl: String? = null,
    val senderGuardAddress: String? = null,
    val senderGuardHostname: String? = null,
    val senderPublicKey: String? = null,
    val refId: String? = null,
    val actionFor: String? = null
)
