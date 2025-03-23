package ro.aenigma.models

import ro.aenigma.models.enums.MessageActionType

data class MessageAction(
    val actionType: MessageActionType? = null,
    val refId: String? = null,
    var senderAddress: String? = null
)