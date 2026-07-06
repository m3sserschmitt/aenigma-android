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

package ro.aenigma.crypto.extensions

import java.security.MessageDigest

object HashExtensions {
    @JvmStatic
    fun ByteArray.getSha256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    @JvmStatic
    fun ByteArray.getSha256Hex(): String {
        return this.getSha256().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    @JvmStatic
    fun String.getSha256Hex(): String {
        return this.toByteArray().getSha256Hex()
    }
}
