package ro.aenigma.models.extensions

import ro.aenigma.models.GroupData
import ro.aenigma.models.GroupMember
import ro.aenigma.util.SerializerExtensions.deepCopy

object GroupDataExtensions {
    @JvmStatic
    fun GroupData?.withName(name: String?): GroupData? {
        return this.deepCopy()?.copy(name = name)
    }

    @JvmStatic
    fun GroupData?.withMembers(members: List<GroupMember>?): GroupData? {
        return this.deepCopy()?.copy(members = members)
    }
}
