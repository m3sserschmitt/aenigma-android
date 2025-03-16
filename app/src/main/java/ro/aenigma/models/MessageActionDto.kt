package ro.aenigma.models

import ro.aenigma.models.enums.MessageActionType

class MessageActionDto(
    val actionType: MessageActionType?,
    val refId: String?
)
