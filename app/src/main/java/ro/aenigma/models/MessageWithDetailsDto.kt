package ro.aenigma.models

import kotlinx.coroutines.flow.MutableStateFlow

data class MessageWithDetailsDto(
    val message: MessageDto,
    val sender: ContactDto?,
    val actionFor: MessageDto?,
    val actionForSender: MutableStateFlow<ContactDto?> = MutableStateFlow(null)
)
