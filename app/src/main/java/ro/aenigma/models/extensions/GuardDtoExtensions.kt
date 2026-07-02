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

import ro.aenigma.data.database.GuardEntity
import ro.aenigma.models.GuardDto
import ro.aenigma.models.ServerInfoDto

object GuardDtoExtensions {
    @JvmStatic
    fun GuardDto.toEntity(): GuardEntity {
        return GuardEntity(
            id = id,
            address = address,
            publicKey = publicKey,
            hostname = hostname,
            onionService = onionService,
            graphVersion = graphVersion,
            dateCreated = dateCreated
        )
    }

    @JvmStatic
    fun GuardDto.toServerInfoDto(): ServerInfoDto {
        return ServerInfoDto(
            address = address,
            onionService = onionService,
            hostname = hostname,
            graphVersion = graphVersion
        )
    }

    @JvmStatic
    fun GuardDto.withNoGraphVersion(): GuardDto {
        return copy(graphVersion = null)
    }

    @JvmStatic
    fun GuardDto.getHostname(): String? {
        return if(hostname.isNullOrBlank()) {
            onionService
        } else {
            hostname
        }
    }
}
