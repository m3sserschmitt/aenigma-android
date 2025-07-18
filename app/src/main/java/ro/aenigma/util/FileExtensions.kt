package ro.aenigma.util

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File

object FileExtensions {
    fun File.toContentUriString(context: Context): String =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            this
        ).toString()
}
