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
    fun String?.isValidPrivateKey(): Boolean {
        return this.isValidKey { privateKeyRegex }
    }

    @JvmStatic
    private val privateKeyRegex: Pattern by lazy {
        Pattern.compile(
            """^-----BEGIN(?: [A-Z]+)* PRIVATE KEY-----\s*([A-Za-z0-9+/=\r\n]+?)\s*-----END(?: [A-Z]+)* PRIVATE KEY-----$""",
            Pattern.MULTILINE
        )
    }

    @JvmStatic
    private val publicKeyRegex: Pattern by lazy {
        Pattern.compile(
            """^-----BEGIN(?: [A-Z]+)* PUBLIC KEY-----\s*([A-Za-z0-9+/=\r\n]+?)\s*-----END(?: [A-Z]+)* PUBLIC KEY-----$""",
            Pattern.MULTILINE
        )
    }
}
