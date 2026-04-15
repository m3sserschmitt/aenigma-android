package ro.aenigma.util

import java.io.File

object FileExtensions {
    fun File.lengthSafe(): Long = runCatching { length() }.getOrDefault(-1L)
}
