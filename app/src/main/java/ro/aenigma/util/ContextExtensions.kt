package ro.aenigma.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import ro.aenigma.ui.AppActivity
import ro.aenigma.util.ContentResolverExtensions.querySize
import androidx.core.net.toUri
import ro.aenigma.R
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_CHUNK_PACKING_SIZE
import ro.aenigma.util.FileExtensions.lengthSafe
import java.io.File
import androidx.core.graphics.createBitmap
import ro.aenigma.util.Constants.Companion.PRIVATE_KEY_FILE
import ro.aenigma.util.Constants.Companion.PUBLIC_KEY_FILE

object ContextExtensions {

    fun Context.getPrivateKeyFile(): File {
        return File(filesDir, PRIVATE_KEY_FILE)
    }

    fun Context.getPublicKeyFile(): File {
        return File(filesDir, PUBLIC_KEY_FILE)
    }

    fun Context.getConversationFilesDir(destinationDir: String): File {
        return File(filesDir, destinationDir)
    }

    fun Context.deleteUri(uri: String): Boolean {
        return try {
            contentResolver.delete(uri.toUri(), null, null) > 0
        } catch (_: Exception) {
            false
        }
    }

    fun Context.getBitmapFromDrawable(
        drawableResId: Int,
        width: Int = 0,
        height: Int = 0
    ): Bitmap? {
        val drawable = ContextCompat.getDrawable(this, drawableResId) ?: return null
        val bitmapWidth = if (width > 0) width else drawable.intrinsicWidth
        val bitmapHeight = if (height > 0) height else drawable.intrinsicHeight
        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    fun Context.isImageUri(uri: String): Boolean {
        return contentResolver.getType(uri.toUri())?.startsWith("image/") == true
    }

    private fun Context.sizeOf(uriString: String): Long {
        val uri = uriString.toUri()

        return when {
            uri.scheme?.equals(ContentResolver.SCHEME_CONTENT, true) == true ->
                contentResolver.querySize(uri)

            uri.scheme?.equals(ContentResolver.SCHEME_FILE, true) == true ->
                File(uri.path ?: return -1).lengthSafe()

            uri.scheme == null -> File(uriString).lengthSafe()

            else -> -1L
        }
    }

    fun Context.splitFilesFirstFitDecreasing(
        uriStrings: List<String>,
        limitBytes: Long = ATTACHMENTS_CHUNK_PACKING_SIZE
    ): List<List<String>> {

        val entries = uriStrings.map { Entry(it, sizeOf(it)) }

        if (entries.any { entry -> entry.size < 0 }) {
            return listOf()
        }

        val sorted = entries.sortedByDescending { it.size }

        val bins = mutableListOf<Bin>()

        for (e in sorted) {
            if (e.size >= limitBytes) {
                bins += Bin(mutableListOf(e.str), e.size)
                continue
            }

            val fit = bins.firstOrNull { it.canFit(e.size, limitBytes) }
            if (fit != null) fit.add(e) else bins += Bin(mutableListOf(e.str), e.size)
        }

        return bins.map { it.items }
    }

    private data class Entry(val str: String, val size: Long)

    private data class Bin(
        val items: MutableList<String>,
        var currentSize: Long = 0
    ) {
        fun canFit(extra: Long, limit: Long) = currentSize + extra < limit
        fun add(e: Entry) {
            items += e.str
            currentSize += e.size
        }
    }

    fun Context.findActivity(): AppActivity {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context as AppActivity
            context = context.baseContext
        }
        throw IllegalStateException("Permissions should be called in the context of an Activity")
    }

    fun Context.openApplicationDetails() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", this.packageName, null)
        )
        this.startActivity(intent)
    }

    fun Context.openUriInExternalApp(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, contentResolver.getType(uri) ?: "*/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_app_to_open), Toast.LENGTH_SHORT).show()
        }
    }
}
