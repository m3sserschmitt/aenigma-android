package ro.aenigma.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

object ContentResolverExtensions {
    fun ContentResolver.querySize(uri: Uri): Long =
        query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use(Cursor::getFirstLong) ?: -1L

    fun ContentResolver.getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && index != -1) {
                    return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment
    }

    fun ContentResolver.getExtension(uri: Uri): String? {
        return getFileName(uri)
            ?.substringAfter('.', "")
            ?.takeIf { ext ->
                ext.isNotBlank()
            }?.lowercase()
    }
}
