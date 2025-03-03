package ro.aenigma.crypto

import android.util.Base64
import ro.aenigma.crypto.Base64Extensions.isValidBase64
import ro.aenigma.crypto.HashExtensions.getSha256
import java.util.regex.Pattern

object PublicKeyExtensions {

    private fun String?.isValidKey(regexProvider: () -> Pattern): Boolean {
        return this.getKeyBase64Content(regexProvider)?.isValidBase64() == true
    }

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
        } catch (e: Exception) {
            null
        }
    }

    fun String?.getPublicKeyBase64(): String? {
        return this.getKeyBase64Content { publicKeyRegex }
    }

    fun String?.getAddressFromPublicKey(): String? {
        try {
            if(this == null)
            {
                return null
            }
            val base64Content = getPublicKeyBase64() ?: return null
            val decodedContent = Base64.decode(base64Content, Base64.DEFAULT) ?: return null
            return decodedContent.getSha256()
        } catch (_: Exception)
        {
            return null
        }
    }

    fun String?.isValidPublicKey(): Boolean {
        return this.isValidKey { publicKeyRegex }
    }

    fun String?.publicKeyMatchAddress(address: String?): Boolean
    {
        if(this == null || address == null)
        {
            return false
        }

        return this.getAddressFromPublicKey() == address
    }

    fun String?.isValidPrivateKey(): Boolean {
        return this.isValidKey { privateKeyRegex }
    }

    private val privateKeyRegex: Pattern by lazy {
        Pattern.compile(
            """^-----BEGIN(?: [A-Z]+)* PRIVATE KEY-----\s*([A-Za-z0-9+/=\r\n]+?)\s*-----END(?: [A-Z]+)* PRIVATE KEY-----$""",
            Pattern.MULTILINE
        )
    }

    private val publicKeyRegex: Pattern by lazy {
        Pattern.compile(
            """^-----BEGIN(?: [A-Z]+)* PUBLIC KEY-----\s*([A-Za-z0-9+/=\r\n]+?)\s*-----END(?: [A-Z]+)* PUBLIC KEY-----$""",
            Pattern.MULTILINE
        )
    }
}
