package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GroupEntity
import ro.aenigma.models.GroupDataDto

object GroupEntityFactory {
    @JvmStatic
    fun create(address: String, groupDataDto: GroupDataDto, resourceUrl: String): GroupEntity {
        return GroupEntity(
            address = address,
            groupData = groupDataDto,
            resourceUrl = resourceUrl
        )
    }
}
