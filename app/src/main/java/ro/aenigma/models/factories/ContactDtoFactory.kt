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

package ro.aenigma.models.factories

import ro.aenigma.models.ContactDto
import ro.aenigma.models.enums.ContactType
import java.time.ZonedDateTime

object ContactDtoFactory {
    @JvmStatic
    fun createContact(
        address: String, name: String?, publicKey: String?, guardHostname: String?,
        guardAddress: String?
    ): ContactDto {
        val dateCreated = ZonedDateTime.now()
        return ContactDto(
            address = address,
            name = name,
            publicKey = publicKey,
            guardHostname = guardHostname,
            guardAddress = guardAddress,
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastMessageId = null,
            dateCreated = dateCreated,
            dateUpdated = dateCreated
        )
    }

    @JvmStatic
    fun createContact(name: String): ContactDto {
        val dateCreated = ZonedDateTime.now()
        return ContactDto(
            address = "",
            name = name,
            publicKey = null,
            guardHostname = null,
            guardAddress = null,
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastMessageId = null,
            dateCreated = dateCreated,
            dateUpdated = dateCreated
        )
    }

    @JvmStatic
    fun createGroup(address: String, name: String?): ContactDto {
        val dateCreated = ZonedDateTime.now()
        return ContactDto(
            address = address,
            name = name,
            publicKey = null,
            guardHostname = null,
            guardAddress = null,
            type = ContactType.GROUP,
            hasNewMessage = false,
            lastMessageId = null,
            dateCreated = dateCreated,
            dateUpdated = dateCreated
        )
    }
}
