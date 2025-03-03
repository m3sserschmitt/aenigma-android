package ro.aenigma.models

import ro.aenigma.util.MessageActionType

class MessageAction(
    val actionType: MessageActionType,
    val refId: String?,
    var senderAddress: String
)