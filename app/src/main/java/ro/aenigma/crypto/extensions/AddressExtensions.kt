package ro.aenigma.crypto.extensions

import java.util.regex.Pattern

object AddressExtensions {
    @JvmStatic
    fun String?.isValidAddress(): Boolean {
        return !this.isNullOrBlank() && addressRegex.matcher(this).matches()
    }

    @JvmStatic
    private val addressRegex: Pattern by lazy {
        Pattern.compile("^[a-f0-9]{64}$")
    }
}
