package ro.aenigma.models

import ro.aenigma.util.MessageActionType

class MessageActionDto(
    val actionType: MessageActionType?,
    val refId: String?
)
