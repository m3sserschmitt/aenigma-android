package ro.aenigma.util

import android.net.Uri
import androidx.core.net.toUri
import ro.aenigma.util.Constants.Companion.SHARE_API_PATH

object UriExtensions {
    @JvmStatic
    fun Uri.isRemote(): Boolean {
        return scheme in listOf("http", "https")
    }

    @JvmStatic
    fun Uri.isSharedData(): Boolean {
        return path.equals(SHARE_API_PATH, ignoreCase = true)
    }

    @JvmStatic
    fun Uri.getArticleUri(): Uri? {
        return try {
            val regex = Regex("[?&]url=([^&#]+)")
            val match = regex.find(toString().lowercase())
            val encodedValue = match?.groups?.get(1)?.value
            val decodedValue = encodedValue?.let { Uri.decode(it) }
            decodedValue?.toUri()
        } catch (_: Exception) {
            null
        }
    }
}
