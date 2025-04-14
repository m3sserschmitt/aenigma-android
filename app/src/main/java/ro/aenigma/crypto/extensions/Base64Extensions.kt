package ro.aenigma.crypto.extensions

import android.util.Base64

object Base64Extensions {
    @JvmStatic
    fun String?.isValidBase64(): Boolean {
        return !this.isNullOrBlank() && try {
            Base64.decode(this, Base64.DEFAULT)
            true
        } catch (_: Exception) {
            false
        }
    }
}
