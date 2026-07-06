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

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import ro.aenigma.util.ContextExtensions.getPrivateKeyFile
import ro.aenigma.util.ContextExtensions.getPublicKeyFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeysManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    companion object {

        @JvmStatic
        private fun writeKey(file: File, data: ByteArray) {
            val outStream = FileOutputStream(file, false)
            outStream.write(data)
            outStream.close()
        }

        @JvmStatic
        private fun readKey(file: File): ByteArray? {
            return try {
                val stream = FileInputStream(file)
                val data = stream.readBytes()
                stream.close()
                data
            } catch (_: Exception) {
                null
            }
        }
    }

    init {
        generateKeyIfNotExistent()
    }

    private fun keysExists(): Boolean {
        return try {
            context.getPrivateKeyFile().exists() && context.getPublicKeyFile().exists()
        } catch (_: Exception) {
            false
        }
    }

    private fun generateKeys(): Boolean {
        return try {
            val keyPairDto = KeysHelper.keyPairToPEM()
            val encryptedPrivateKey =
                CryptoProvider.masterKeyEncrypt(
                    keyPairDto.privateKey?.toByteArray() ?: return false
                ) ?: return false
            writeKey(context.getPrivateKeyFile(), encryptedPrivateKey)
            writeKey(
                context.getPublicKeyFile(),
                keyPairDto.publicKey?.toByteArray() ?: return false
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun readPrivateKey(): ByteArray? {
        return try {
            CryptoProvider.masterKeyDecrypt(readKey(context.getPrivateKeyFile()) ?: return null)
                ?: return null
        } catch (_: Exception) {
            null
        }
    }

    fun readPublicKey(): String? {
        return try {
            String(readKey(context.getPublicKeyFile()) ?: return null)
        } catch (_: Exception) {
            null
        }
    }

    private fun generateKeyIfNotExistent(): Boolean {
        return if (!keysExists()) {
            generateKeys()
        } else {
            true
        }
    }
}
