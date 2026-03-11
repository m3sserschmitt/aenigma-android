package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.GroupEntity
import ro.aenigma.models.GroupDto

object GroupEntityExtensions {
    @JvmStatic
    fun GroupEntity.toDto(): GroupDto {
        return GroupDto(
            address = address,
            groupData = groupData,
            resourceUrl = resourceUrl
        )
    }
}
