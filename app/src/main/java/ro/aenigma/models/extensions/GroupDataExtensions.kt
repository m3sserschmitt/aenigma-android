package ro.aenigma.models.extensions

import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.GroupDataDto

object GroupDataExtensions {
    @JvmStatic
    fun GroupDataDto.withName(name: String?): GroupDataDto {
        return copy(name = name)
    }

    @JvmStatic
    fun GroupDataDto.withMembers(members: List<ExportedContactDataDto>?): GroupDataDto {
        return copy(members = members)
    }

    @JvmStatic
    fun GroupDataDto.removeMember(address: String): GroupDataDto {
        val filteredMembers = members?.filter { item -> item.address != address }
        return withMembers(filteredMembers)
    }

    @JvmStatic
    fun GroupDataDto.removeMembers(addresses: List<String>): GroupDataDto {
        val set = addresses.toSet()
        return withMembers(members?.filter { member -> !set.contains(member.address) })
    }

    @JvmStatic
    fun GroupDataDto.iAmAdmin(localAddress: String): Boolean {
        return admins?.contains(localAddress) == true
    }

    @JvmStatic
    fun GroupDataDto.incrementNonce(): GroupDataDto {
        val newNonce = (nonce ?: 0) + 1
        return copy(nonce = newNonce)
    }
}
