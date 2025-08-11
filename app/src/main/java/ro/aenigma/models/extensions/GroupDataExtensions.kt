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
        val filteredMembers = members?.filter { item -> item.address != address }
        return withMembers(filteredMembers)
    }

    @JvmStatic
    fun GroupData.removeMembers(addresses: List<String>): GroupData? {
        val set = addresses.toSet()
        return withMembers(members?.filter { member -> !set.contains(member.address) })
    }

    @JvmStatic
    fun GroupData.iAmAdmin(localAddress: String): Boolean {
        return admins?.contains(localAddress) == true
    }

    @JvmStatic
    fun GroupData.incrementNonce(): GroupData? {
        val newNonce = (nonce ?: 0) + 1
        return deepCopy()?.copy(nonce = newNonce)
    }
}
