package ro.aenigma.util

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns

object ContentResolverExtensions {
    fun ContentResolver.querySize(uri: Uri): Long =
        query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use(Cursor::getFirstLong) ?: -1L
}
