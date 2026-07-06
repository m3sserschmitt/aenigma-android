/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
