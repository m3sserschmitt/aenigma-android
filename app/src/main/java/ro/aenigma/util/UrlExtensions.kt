package ro.aenigma.util

import androidx.core.net.toUri

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
