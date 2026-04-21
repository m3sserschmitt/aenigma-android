package ro.aenigma.util

import android.net.Uri

object UriExtensions {
    @JvmStatic
    fun Uri.isRemote(): Boolean {
        return scheme in listOf("http", "https")
    }
}
