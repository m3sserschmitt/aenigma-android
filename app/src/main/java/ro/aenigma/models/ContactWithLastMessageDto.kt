package ro.aenigma.models

data class ContactWithLastMessageDto(
    val contact: ContactDto,
    val lastMessage: MessageDto?
)
