package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GroupEntity
import ro.aenigma.models.GroupData

class GroupEntityFactory {
    companion object {
        @JvmStatic
        fun create(address: String, groupData: GroupData, resourceUrl: String): GroupEntity {
            return GroupEntity(address = address, groupData = groupData, resourceUrl = resourceUrl)
        }
    }
}
