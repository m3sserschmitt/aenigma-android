package ro.aenigma.crypto

import android.util.Base64
import ro.aenigma.crypto.Base64Extensions.isValidBase64
import ro.aenigma.crypto.PublicKeyExtensions.isValidPublicKey

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

fun String?.getStringDataFromSignature(publicKey: String): String? {
    return try {
        this.getDataFromSignature(publicKey)?.toString(Charsets.UTF_8)
    }catch (_: Exception)
    {
        null
    }
}
