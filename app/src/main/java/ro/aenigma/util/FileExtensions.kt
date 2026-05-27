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
