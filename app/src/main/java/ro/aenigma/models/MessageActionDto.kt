package ro.aenigma.models

import ro.aenigma.models.enums.MessageActionType

data class MessageActionDto(
    val actionType: MessageActionType? = null,
    val refId: String? = null
)
