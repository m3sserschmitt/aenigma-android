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

import ro.aenigma.data.database.VertexEntity
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.VertexDto
import ro.aenigma.models.extensions.NeighborhoodExtensions.hasHost

object VertexDtoExtensions {
    @JvmStatic
    fun VertexDto.toEntity(): VertexEntity {
        return VertexEntity(
            publicKey = publicKey ?: "",
            address = neighborhood?.address ?: "",
            hostname = neighborhood?.hostname,
            onionService = neighborhood?.onionService
        )
    }

    @JvmStatic
    fun VertexDto.toServerInfoDto(): ServerInfoDto {
        return ServerInfoDto(
            address = address,
            hostname = neighborhood?.hostname,
            onionService = neighborhood?.onionService,
            graphVersion = null
        )
    }

    @JvmStatic
    fun VertexDto.hasHost(targetHost: String): Boolean {
        return neighborhood?.hasHost(targetHost) == true
    }
}
