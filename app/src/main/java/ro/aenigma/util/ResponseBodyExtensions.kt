package ro.aenigma.util

import okhttp3.ResponseBody
import java.io.File

object ResponseBodyExtensions {
    fun ResponseBody.saveToFile(file: File) {
        byteStream().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
