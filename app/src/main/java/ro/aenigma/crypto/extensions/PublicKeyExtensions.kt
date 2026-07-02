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

import android.util.Base64
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.HashExtensions.getSha256Hex
import java.util.regex.Pattern

object PublicKeyExtensions {
    @JvmStatic
    private fun String?.isValidKey(regexProvider: () -> Pattern): Boolean {
        return this.getKeyBase64Content(regexProvider)?.isValidBase64() == true
    }

    @JvmStatic
    private fun String?.getKeyBase64Content(regexProvider: () -> Pattern): String? {
        return try {
            if (this.isNullOrBlank()) {
                return null
            }

            val matcher = regexProvider.invoke().matcher(this)

            if (matcher.find()) {
                matcher.group(1)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun String?.getPublicKeyBase64(): String? {
        return this.getKeyBase64Content { publicKeyRegex }
    }

    @JvmStatic
    fun String?.getAddressFromPublicKey(): String? {
        try {
            if(this == null)
            {
                return null
            }
            val base64Content = getPublicKeyBase64() ?: return null
            val decodedContent = Base64.decode(base64Content, Base64.DEFAULT) ?: return null
            return decodedContent.getSha256Hex()
        } catch (_: Exception)
        {
            return null
        }
    }

    @JvmStatic
    fun String?.isValidPublicKey(): Boolean {
        return this.isValidKey { publicKeyRegex }
    }

    @JvmStatic
    fun String?.publicKeyMatchAddress(address: String?): Boolean
    {
        if(this == null || address == null)
        {
            return false
        }

        return this.getAddressFromPublicKey() == address
    }

    @JvmStatic
    private val publicKeyRegex: Pattern by lazy {
        Pattern.compile(
            """^-----BEGIN(?: [A-Z]+)* PUBLIC KEY-----\s*([A-Za-z0-9+/=\r\n]+?)\s*-----END(?: [A-Z]+)* PUBLIC KEY-----$""",
            Pattern.MULTILINE
        )
    }
}
