package ro.aenigma.models

import ro.aenigma.models.enums.MessageActionType

class MessageAction(
    val actionType: MessageActionType,
    val refId: String?,
    var senderAddress: String
)