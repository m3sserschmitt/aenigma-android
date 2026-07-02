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
