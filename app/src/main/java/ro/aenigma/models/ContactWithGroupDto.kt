package ro.aenigma.models

data class ContactWithGroupDto(
    val contact: ContactDto,
    val group: GroupDto?
)
