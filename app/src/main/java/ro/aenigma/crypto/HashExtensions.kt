package ro.aenigma.crypto

import java.security.MessageDigest

object HashExtensions {
    fun ByteArray.getSha256(): String {
        return MessageDigest.getInstance("SHA-256").digest(this)
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun String.getSha256(): String {
        return this.toByteArray().getSha256()
    }
}
