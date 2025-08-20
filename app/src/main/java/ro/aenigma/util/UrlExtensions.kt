package ro.aenigma.util

import android.webkit.MimeTypeMap
import androidx.core.net.toUri

object UrlExtensions {
    fun String.isImageUrlByExtension(): Boolean {
        if(!this.isRemoteUri())
        {
            return false
        }
        val extension = MimeTypeMap.getFileExtensionFromUrl(this)

        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension)
            ?.let { if (it.startsWith("image/", ignoreCase = true)) return true }

        val otherExtensions = setOf(
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg", "heic", "heif",
            "tif", "tiff", "ico", "avif", "apng", "jfif"
        )
        return extension in otherExtensions
    }

    fun String.getBaseUrl(): String {
        val uri = this.toUri()
        val port = if (uri.port != -1) ":${uri.port}" else ""
        return "${uri.scheme}://${uri.host}$port"
    }

    fun String.getQueryParameter(key: String): String? {
        val uri = this.toUri()
        val matchingKey = uri.queryParameterNames.find { item ->
            item.equals(key, ignoreCase = true)
        }
        return matchingKey?.let { value ->
            uri.getQueryParameter(value)
        }
    }

    fun String.getTagQueryParameter(): String? {
        return this.getQueryParameter("tag")
    }

    fun String?.isRemoteUri(): Boolean {
        return this?.toUri()?.scheme in listOf("http", "https")
    }
}
