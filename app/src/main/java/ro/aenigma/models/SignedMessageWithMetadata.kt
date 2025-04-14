package ro.aenigma.models

import ro.aenigma.models.enums.MessageType

class SignedMessageWithMetadata(
    text: String? = null,
    type: MessageType? = null,
    senderName: String? = null,
    groupResourceUrl: String? = null,
    senderGuardAddress: String? = null,
    senderGuardHostname: String? = null,
    senderPublicKey: String? = null,
    refId: String? = null,
    actionFor: String? = null,
    val signature: String? = null
): MessageWithMetadata(
    text = text,
    type = type,
    senderName = senderName,
    groupResourceUrl = groupResourceUrl,
    senderGuardAddress = senderGuardAddress,
    senderGuardHostname = senderGuardHostname,
    senderPublicKey = senderPublicKey,
    refId = refId,
    actionFor = actionFor
)
