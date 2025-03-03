package ro.aenigma.util

import android.net.Uri

fun String.getBaseUrl(): String {
    val uri = Uri.parse(this)
    val port = if (uri.port != -1) ":${uri.port}" else ""
    return "${uri.scheme}://${uri.host}$port"
}

fun String.getQueryParameter(key: String): String? {
    val uri = Uri.parse(this)
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
