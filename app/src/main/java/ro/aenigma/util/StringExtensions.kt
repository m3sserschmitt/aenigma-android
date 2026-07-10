/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.util

import androidx.core.net.toUri
import com.fasterxml.jackson.module.kotlin.readValue
import org.erdtman.jcs.JsonCanonicalizer
import ro.aenigma.util.SerializerExtensions.createJsonMapper
import ro.aenigma.util.UriExtensions.isRemote
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

    @OptIn(ExperimentalContracts::class)
    fun String?.isDomain(): Boolean {
        contract {
            returns(true) implies (this@isDomain != null)
        }
        return try {
            val domainRegex = Regex("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?::\\d+)?$")
            return domainRegex.matches(this ?: return false)
        } catch (_: Exception) {
            false
        }
    }

    @OptIn(ExperimentalContracts::class)
    fun String?.isOnionAddress(): Boolean {
        contract {
            returns(true) implies (this@isOnionAddress != null)
        }
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

    @OptIn(ExperimentalContracts::class)
    fun String?.isRemoteUri(): Boolean {
        contract {
            returns(true) implies (this@isRemoteUri != null)
        }
        return this?.toUri()?.isRemote() == true
    }

    fun String?.isRemoteImageUri(): Boolean {
        val imageExtensions =
            setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic", "heif", "tiff", "avif")

        val path = try {
            java.net.URI(this).path ?: this
        } catch (_: Exception) {
            this
        } ?: return false

        val lastSegment = path.substringAfterLast('/')
        val extension = lastSegment.substringAfterLast('.', "").lowercase()

        return extension.isNotEmpty() && extension in imageExtensions
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

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isImageMime(): Boolean {
        contract {
            returns(true) implies (this@isImageMime != null)
        }
        return this?.startsWith("image/", ignoreCase = true) == true
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isVideoMime(): Boolean {
        contract {
            returns(true) implies (this@isVideoMime != null)
        }
        return this?.startsWith("video/", ignoreCase = true) == true
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isAudioMime(): Boolean {
        contract {
            returns(true) implies (this@isAudioMime != null)
        }
        return this?.startsWith("audio/", ignoreCase = true) == true
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isPdfMime(): Boolean {
        contract {
            returns(true) implies (this@isPdfMime != null)
        }
        return this?.lowercase() == "application/pdf"
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isMarkdownMime(): Boolean {
        contract {
            returns(true) implies (this@isMarkdownMime != null)
        }
        return this?.lowercase() == "text/markdown"
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isApkMime(): Boolean {
        contract {
            returns(true) implies (this@isApkMime != null)
        }
        return this?.lowercase() == "application/vnd.android.package-archive"
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isArchiveMime(): Boolean {
        contract {
            returns(true) implies (this@isArchiveMime != null)
        }
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

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isJsonMime(): Boolean {
        contract {
            returns(true) implies (this@isJsonMime != null)
        }
        return this?.lowercase() == "application/json"
    }

    @OptIn(ExperimentalContracts::class)
    @JvmStatic
    fun String?.isTextMime(): Boolean {
        contract {
            returns(true) implies (this@isTextMime != null)
        }
        return this?.startsWith("text/", ignoreCase = true) == true
    }
}
