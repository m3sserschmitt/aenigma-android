package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.GroupEntityExtensions.toDto
import ro.aenigma.models.ContactWithGroupDto

object ContactWithGroupEntityExtensions {
    @JvmStatic
    fun ContactWithGroup.toDto(): ContactWithGroupDto {
        return ContactWithGroupDto(
            contact = contact.toDto(),
            group = group?.toDto()
        )
    }
}
