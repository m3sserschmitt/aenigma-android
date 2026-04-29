package ro.aenigma.util

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class BufferedRequestBody(
    private val file: File,
    private val mediaType: MediaType,
    private val bufferSize: Int = Constants.REQUEST_BODY_DEFAULT_BUFFER_SIZE,
    private val onProgress: (percent: Int) -> Unit = { }
) : RequestBody() {

    override fun contentType(): MediaType = mediaType

    @Throws(IOException::class)
    override fun contentLength(): Long = file.length()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val totalBytes = file.length()
        val buffer = ByteArray(bufferSize)
        var uploaded = 0.0

        FileInputStream(file).use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                onProgress.invoke(((uploaded / totalBytes) * 100).toInt())
            }
        }
    }
}
