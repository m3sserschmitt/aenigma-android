package ro.aenigma.crypto.extensions

import java.security.MessageDigest

object HashExtensions {
    @JvmStatic
    fun ByteArray.getSha256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    @JvmStatic
    fun String.getSha256(): ByteArray {
        return this.toByteArray().getSha256()
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
