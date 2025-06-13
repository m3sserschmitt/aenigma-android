package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.GroupEntity
import ro.aenigma.models.extensions.GroupDataExtensions.removeMember
import ro.aenigma.util.SerializerExtensions.deepCopy

object GroupEntityExtensions {
    @JvmStatic
    fun GroupEntity.removeMember(address: String): GroupEntity? {
        val newGroupData = groupData.removeMember(address) ?: return null
        return this.deepCopy()?.copy(groupData = newGroupData)
    }
}
