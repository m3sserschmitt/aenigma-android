package ro.aenigma.crypto.extensions

import android.util.Base64
import ro.aenigma.crypto.CryptoProvider
import ro.aenigma.crypto.extensions.Base64Extensions.isValidBase64
import ro.aenigma.crypto.extensions.PublicKeyExtensions.isValidPublicKey
import ro.aenigma.crypto.services.SignatureService
import ro.aenigma.models.SignedData
import ro.aenigma.util.SerializerExtensions.fromJson
import ro.aenigma.util.SerializerExtensions.toCanonicalJson

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
    inline fun <reified T> T.sign(signatureService: SignatureService): SignedData? {
        return try {
            val serializedArtifact = this.toCanonicalJson() ?: return null
            val signature = signatureService.sign(serializedArtifact.toByteArray())
            SignedData(signedData = signature.signedData, publicKey = signature.publicKey)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    inline fun <reified T> SignedData.verify(): T? {
        try {
            if (!CryptoProvider.verifyEx(
                    this.publicKey ?: return null,
                    this.signedData ?: return null
                )
            ) {
                return null
            }
            val data = this.signedData.getStringDataFromSignature(this.publicKey) ?: return null
            val canonicalData = data.toCanonicalJson()
            if (data != canonicalData) {
                return null
            }
            return data.fromJson()
        } catch (_: Exception) {
            return null
        }
    }
}
