package ro.aenigma.models.extensions

import ro.aenigma.data.database.GroupEntity
import ro.aenigma.models.GroupDto
import ro.aenigma.models.extensions.GroupDataExtensions.removeMember

object GroupDtoExtensions {
    @JvmStatic
    fun GroupDto.removeMember(address: String): GroupDto {
        val newGroupData = groupData.removeMember(address)
        return copy(groupData = newGroupData)
    }

    @JvmStatic
    fun GroupDto.toEntity(): GroupEntity {
        return GroupEntity(
            address = address,
            groupData = groupData,
            resourceUrl = resourceUrl
        )
    }
}
