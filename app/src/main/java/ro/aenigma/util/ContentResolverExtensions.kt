package ro.aenigma.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ContentResolverExtensions {
    suspend fun ContentResolver.querySize(uri: Uri): Long {
        return withContext(Dispatchers.IO) {
            query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use(Cursor::getFirstLong) ?: -1L
        }
    }

    suspend fun ContentResolver.getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            return withContext(Dispatchers.IO) {
                query(uri, null, null, null, null)?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && index != -1) {
                        cursor.getString(index)
                    } else {
                        null
                    }
                }
            }
        }
        return uri.lastPathSegment
    }

    suspend fun ContentResolver.getExtension(uri: Uri): String? {
        return getFileName(uri)
            ?.substringAfter('.', "")
            ?.takeIf { ext ->
                ext.isNotBlank()
            }?.lowercase()
    }
}
