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

import ro.aenigma.crypto.extensions.HashExtensions.getSha256Hex
import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.GroupDataDto
import java.util.UUID

object GroupDataFactory {
    @JvmStatic
    fun create(name: String, members: List<ExportedContactDataDto>, admins: List<String>): GroupDataDto {
        return GroupDataDto(
            address = UUID.randomUUID().toString().getSha256Hex(),
            name = name,
            members = members,
            admins = admins,
            nonce = 1
        )
    }
}
