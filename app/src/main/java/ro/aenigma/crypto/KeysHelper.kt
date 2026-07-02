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

package ro.aenigma.crypto

import ro.aenigma.util.Constants.Companion.KEY_SIZE_BITS
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import ro.aenigma.models.KeyPairDto
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.io.StringWriter
import java.security.Key

class KeysHelper {

    companion object {

        private const val KEYS_ALGORITHM = "RSA"

        @JvmStatic
        fun generateKeyPair(): KeyPair? {
            return try {
                val keyPairGenerator = KeyPairGenerator.getInstance(KEYS_ALGORITHM)
                keyPairGenerator.initialize(KEY_SIZE_BITS)
                keyPairGenerator.generateKeyPair()
            } catch (_: Exception) {
                null
            }
        }

        @JvmStatic
        private fun toPEM(key: Key): String {
            val stringWriter = StringWriter()
            val jcaWriter = JcaPEMWriter(stringWriter)
            jcaWriter.writeObject(key)
            jcaWriter.close()
            return stringWriter.toString()
        }

        @JvmStatic
        fun keyPairToPEM(): KeyPairDto {
            return try {
                val keyPair = generateKeyPair()
                KeyPairDto(
                    publicKey = toPEM(keyPair?.public ?: throw Exception()),
                    privateKey = toPEM(keyPair.private ?: throw Exception())
                )
            } catch (_: Exception) {
                KeyPairDto(publicKey = null, privateKey = null)
            }
        }
    }
}
