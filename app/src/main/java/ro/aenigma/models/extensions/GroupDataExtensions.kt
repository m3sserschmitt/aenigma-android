package ro.aenigma.models.extensions

import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.GroupData
import ro.aenigma.util.SerializerExtensions.deepCopy

object GroupDataExtensions {
    @JvmStatic
    fun GroupData?.withName(name: String?): GroupData? {
        return deepCopy()?.copy(name = name)
    }

    @JvmStatic
    fun GroupData?.withMembers(members: List<ExportedContactData>?): GroupData? {
        return deepCopy()?.copy(members = members)
    }

    @JvmStatic
    fun GroupData.removeMember(address: String): GroupData? {
        var filteredMembers = members?.filter { item -> item.address != address }
        return withMembers(filteredMembers)
    }

    @JvmStatic
    fun GroupData.removeMembers(addresses: List<String>): GroupData? {
        var result: GroupData? = this
        addresses.forEach {  address -> result = removeMember(address) }
        return result
    }

    @JvmStatic
    fun GroupData.iAmAdmin(localAddress: String): Boolean {
        return admins?.contains(localAddress) == true
    }
}
