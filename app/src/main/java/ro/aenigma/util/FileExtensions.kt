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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import java.io.File

object FileExtensions {
    suspend fun File.lengthSafe(): Long {
        return withContext(Dispatchers.IO) {
            runCatching { length() }.getOrDefault(-1L)
        }
    }

    fun File.asBufferedRequestBody(
        mediaType: MediaType,
        bufferSize: Int = Constants.REQUEST_BODY_DEFAULT_BUFFER_SIZE,
        onProgress: (percent: Int) -> Unit = { }
    ): RequestBody {
        return BufferedRequestBody(this, mediaType, bufferSize, onProgress)
    }

    fun File.asBufferedRequestBody(
        mediaType: String,
        bufferSize: Int = Constants.REQUEST_BODY_DEFAULT_BUFFER_SIZE,
        onProgress: (percent: Int) -> Unit = { }
    ): RequestBody {
        return asBufferedRequestBody(mediaType.toMediaType(), bufferSize, onProgress)
    }

    fun File.asBufferedRequestBody(
        mediaType: String,
        usingTor: Boolean,
        onProgress: (percent: Int) -> Unit = { }
    ): RequestBody {
        return asBufferedRequestBody(
            mediaType, if (usingTor) {
                Constants.REQUEST_BODY_BUFFER_SIZE_OVER_TOR
            } else {
                Constants.REQUEST_BODY_DEFAULT_BUFFER_SIZE
            },
            onProgress
        )
    }
}
