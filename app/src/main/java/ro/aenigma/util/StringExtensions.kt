package ro.aenigma.util

import androidx.core.net.toUri
import com.fasterxml.jackson.module.kotlin.readValue
import org.erdtman.jcs.JsonCanonicalizer
import ro.aenigma.util.SerializerExtensions.createJsonMapper

object StringExtensions {

    fun String?.getBaseUrl(): String? {
        return try {
            val uri = this?.toUri() ?: return null
            val port = if (uri.port != -1) ":${uri.port}" else ""
            return "${uri.scheme}://${uri.host}$port"
        } catch (_:Exception) {
            null
        }
    }

    fun String?.getHost(): String? {
        return try {
            this?.toUri()?.host
        } catch (_: Exception) {
            null
        }
    }

    fun String?.getHttpUri(): String? {
        return try {
            val uri = this?.trimSlashAndSpace()?.toUri() ?: return null
            return if (uri.scheme != null && uri.host != null) {
                uri.toString()
            } else if (isOnionAddress()) {
                "http://$this"
            } else if (isDomain()) {
                "https://$this"
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun String.getHttpUri(path: String): String? {
        return "${this.getHttpUri()?.trimSlashAndSpace() ?: return null}/${path.trimSlashAndSpace()}"
    }

    fun String?.trimSlashAndSpace(): String? {
        return this?.trim(' ', '/')
    }

    fun String?.isDomain(): Boolean {
        return try {
            val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?::\\d+)?$")
            return domainRegex.matches(this ?: return false)
        } catch (_: Exception) {
            false
        }
    }

    fun String?.isOnionAddress(): Boolean {
        return try {
            val regex = Regex("^[a-z2-7]{56}\\.onion(?::\\d+)?$")
            return regex.matches(this ?: return false)
        } catch (_: Exception) {
            false
        }
    }

    fun String?.getQueryParameter(key: String): String? {
        return try {
            val uri = this?.toUri() ?: return null
            val matchingKey = uri.queryParameterNames.find { item ->
                item.equals(key, ignoreCase = true)
            }
            matchingKey?.let { value ->
                uri.getQueryParameter(value)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun String?.getTagQueryParameter(): String? {
        return this.getQueryParameter("tag")
    }

    fun String?.isRemoteUri(): Boolean {
        return try {
            this?.toUri()?.scheme in listOf("http", "https")
        } catch (_: Exception) {
            false
        }
    }

    @JvmStatic
    inline fun <reified T> String?.fromJson(): T? {
        return try {
            this?.let { createJsonMapper().readValue<T>(it) }
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun String?.canonicalize(): String? {
        return try {
            JsonCanonicalizer(this ?: return null).encodedString
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun String?.isImageMime(): Boolean {
        return this?.startsWith("image/", ignoreCase = true) == true
    }

    @JvmStatic
    fun String?.isVideoMime(): Boolean {
        return this?.startsWith("video/", ignoreCase = true) == true
    }

    @JvmStatic
    fun String?.isAudioMime(): Boolean {
        return this?.startsWith("audio/", ignoreCase = true) == true
    }

    @JvmStatic
    fun String?.isPdfMime(): Boolean {
        return this?.lowercase() == "application/pdf"
    }

    @JvmStatic
    fun String?.isMarkdownMime(): Boolean {
        return this?.lowercase() == "text/markdown"
    }

    @JvmStatic
    fun String?.isApkMime(): Boolean {
        return this?.lowercase() == "application/vnd.android.package-archive"
    }

    @JvmStatic
    fun String?.isArchiveMime(): Boolean {
        return this?.lowercase() in setOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-7z-compressed",
            "application/x-rar-compressed",
            "application/vnd.rar",
            "application/x-tar",
            "application/gzip"
        )
    }

    @JvmStatic
    fun String?.isJsonMime(): Boolean {
        return this?.lowercase() == "application/json"
    }

    @JvmStatic
    fun String?.isTextMime(): Boolean {
        return this?.startsWith("text/", ignoreCase = true) == true
    }
}
