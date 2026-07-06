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

import ro.aenigma.crypto.extensions.PublicKeyExtensions.getAddressFromPublicKey
import ro.aenigma.models.ExportedContactDataDto

object ExportedContactDataFactory {
    @JvmStatic
    fun create(
        name: String?,
        publicKey: String?,
        guardAddress: String?,
        guardHostname: String?
    ): ExportedContactDataDto {
        return ExportedContactDataDto(
            name = name,
            publicKey = publicKey,
            address = publicKey.getAddressFromPublicKey(),
            guardHostname = guardHostname,
            guardAddress = guardAddress
        )
    }

    @JvmStatic
    fun create(address: String): ExportedContactDataDto {
        return ExportedContactDataDto(
            address = address,
            name = null,
            publicKey = null,
            guardHostname = null,
            guardAddress = null
        )
    }
}
