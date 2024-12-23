package ro.aenigma.crypto

import java.util.regex.Pattern

object AddressExtensions {
    fun String?.isValidAddress(): Boolean {
        return !this.isNullOrBlank() && addressRegex.matcher(this).matches()
    }

    private val addressRegex: Pattern by lazy {
        Pattern.compile("^[a-f0-9]{64}$")
    }
}
