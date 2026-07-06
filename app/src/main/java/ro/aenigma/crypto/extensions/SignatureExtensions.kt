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
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.SignatureDto
import ro.aenigma.util.SerializerExtensions.toCanonicalJson
import ro.aenigma.util.StringExtensions.canonicalize
import ro.aenigma.util.StringExtensions.fromJson

object SignatureExtensions {
    @JvmStatic
    fun String?.getDataFromSignature(publicKey: String): ByteArray? {
        if (!publicKey.isValidPublicKey() || !this.isValidBase64()) {
            return null
        }

        val decodedData = Base64.decode(this, Base64.DEFAULT) ?: return null
        val digestSize = CryptoProvider.getPublicKeySize(publicKey)

        if (decodedData.size < digestSize + 1) {
            return null
        }

        return decodedData.sliceArray(0 until decodedData.size - digestSize)
    }

    @JvmStatic
    fun String?.getStringDataFromSignature(publicKey: String): String? {
        return try {
            this.getDataFromSignature(publicKey)?.toString(Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    suspend inline fun <reified T> T.jsonSign(signatureService: SignatureService): SignatureDto? {
        return try {
            val serializedArtifact = this.toCanonicalJson() ?: return null
            val signature = signatureService.sign(serializedArtifact.toByteArray())
            SignatureDto(signedData = signature.signedData, publicKey = signature.publicKey)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    inline fun <reified T> SignatureDto.jsonVerify(): T? {
        try {
            if (!CryptoProvider.verifyEx(
                    this.publicKey ?: return null,
                    this.signedData ?: return null
                )
            ) {
                return null
            }
            val data = this.signedData.getStringDataFromSignature(this.publicKey) ?: return null
            val canonicalData = data.canonicalize()
            if (data != canonicalData) {
                return null
            }
            return data.fromJson()
        } catch (_: Exception) {
            return null
        }
    }
}
