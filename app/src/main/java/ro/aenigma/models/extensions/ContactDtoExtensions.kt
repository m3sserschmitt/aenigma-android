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

import ro.aenigma.data.database.ContactEntity
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ExportedContactDataDto
import java.time.ZonedDateTime

object ContactDtoExtensions {
    @JvmStatic
    fun ContactDto.toEntity(): ContactEntity = ContactEntity(
        address = address,
        name = name,
        publicKey = publicKey,
        guardHostname = guardHostname,
        guardAddress = guardAddress,
        lastMessageId = lastMessageId,
        hasNewMessage = hasNewMessage,
        type = type,
        dateCreated = dateCreated,
        dateUpdated = dateUpdated
    )

    @JvmStatic
    fun ContactDto.toExportedContactDataDto(): ExportedContactDataDto {
        return ExportedContactDataDto(
            name = name,
            publicKey = publicKey,
            guardAddress = guardAddress,
            guardHostname = guardHostname,
            address = address
        )
    }

    @JvmStatic
    fun ContactDto.withLastMessageId(lastMessageId: Long?): ContactDto {
        return copy(lastMessageId = lastMessageId)
    }

    @JvmStatic
    fun ContactDto.withName(name: String?): ContactDto {
        return copy(name = name, dateUpdated = ZonedDateTime.now())
    }

    @JvmStatic
    fun ContactDto.withNewMessage(): ContactDto {
        return copy(hasNewMessage = true)
    }

    @JvmStatic
    fun ContactDto.withGuardAddress(guardAddress: String?): ContactDto {
        return copy(guardAddress = guardAddress, dateUpdated = ZonedDateTime.now())
    }

    @JvmStatic
    fun ContactDto.withGuardHostname(guardHostname: String?): ContactDto {
        return copy(guardHostname = guardHostname, dateUpdated = ZonedDateTime.now())
    }
}
