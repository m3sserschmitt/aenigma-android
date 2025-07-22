package ro.aenigma.models

data class MessageWithDetailsDto(
    val message: MessageDto,
    val sender: ContactDto?,
    val actionFor: MessageDto?
)
