package ro.aenigma.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import ro.aenigma.activities.AppActivity
import ro.aenigma.util.ContentResolverExtensions.querySize
import androidx.core.net.toUri
import ro.aenigma.R
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_CHUNK_PACKING_SIZE
import ro.aenigma.util.FileExtensions.lengthSafe
import java.io.File
import androidx.core.graphics.createBitmap
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.load
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.stfalcon.imageviewer.StfalconImageViewer
import okhttp3.OkHttpClient
import ro.aenigma.models.MessageDto
import ro.aenigma.util.Constants.Companion.COIL_MEMORY_CACHE_PERCENTAGE
import ro.aenigma.util.Constants.Companion.IMAGES_CACHE_DIR
import ro.aenigma.util.Constants.Companion.PRIVATE_KEY_FILE
import ro.aenigma.util.Constants.Companion.PUBLIC_KEY_FILE
import ro.aenigma.util.UrlExtensions.isImageUrlByExtension
import ro.aenigma.util.UrlExtensions.isRemoteUri

object ContextExtensions {

    fun Context.createImageLoader(client: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(this).memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(this, COIL_MEMORY_CACHE_PERCENTAGE)
                .build()
        }.diskCache {
            DiskCache.Builder()
                .directory(getImagesCacheDir())
                .build()
        }.components {
            add(
                OkHttpNetworkFetcherFactory(
                    callFactory = {
                        client
                    }
                ))
            if (Build.VERSION.SDK_INT >= 28) {
                add(AnimatedImageDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    }

    fun Context.getFileTypeIcon(uri: String): Int {
        return try {
            val mime = contentResolver.getType(uri.toUri())
            when {
                uri.isRemoteUri() -> R.drawable.ic_link
                mime == null -> R.drawable.ic_unknown_document
                mime.startsWith("image/") -> R.drawable.ic_photo
                mime.startsWith("video/") -> R.drawable.ic_video_file
                mime.startsWith("audio/") -> R.drawable.ic_audio_file
                mime == "application/pdf" -> R.drawable.ic_pdf
                mime == "application/vnd.android.package-archive" -> R.drawable.ic_apk_file
                mime.contains("zip") || mime.contains("rar") ||
                        mime.contains("7z") || mime.contains("tar") ||
                        mime.contains("gz") -> R.drawable.ic_zip

                else -> R.drawable.ic_docs_file
            }
        } catch (_: Exception) {
            R.drawable.ic_unknown_document
        }
    }

    fun Context.getPrivateKeyFile(): File {
        return File(filesDir, PRIVATE_KEY_FILE)
    }

    fun Context.getPublicKeyFile(): File {
        return File(filesDir, PUBLIC_KEY_FILE)
    }

    fun Context.getConversationFilesDir(destinationDir: String): File {
        return File(filesDir, destinationDir)
    }

    fun Context.getImagesCacheDir(): File {
        return File(cacheDir, IMAGES_CACHE_DIR)
    }

    fun Context.deleteUri(uri: String): Boolean {
        return try {
            contentResolver.delete(uri.toUri(), null, null) > 0
        } catch (_: Exception) {
            false
        }
    }

    fun Context.isImageUri(uri: String): Boolean {
        return uri.isNotBlank() && contentResolver.getType(uri.toUri())?.startsWith("image/") == true
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
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (uri.scheme == "content") {
                    setDataAndType(uri, contentResolver.getType(uri) ?: "*/*")
                }
            }
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                getString(R.string.no_app_to_open),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Context.shareText(text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, text)
            this.startActivity(
                Intent.createChooser(
                    intent,
                    this.getString(R.string.share_via)
                )
            )
        } catch (_: Exception) {
            Toast.makeText(
                this,
                this.getString(R.string.failed_to_share),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Context.copyToClipboard(text: String) {
        try {
            val clipboard = this.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager
            val data = ClipData.newPlainText("", text)
            clipboard.setPrimaryClip(data)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                this.getString(R.string.failed_to_copy_to_clipboard),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun Context.showImageViewer(message: MessageDto) {
        val uris = (if (message.files.isNullOrEmpty()) message.filesLate.value else message.files)
            .filter { item -> isImageUri(item) && !item.isRemoteUri() }
        if(uris.isNotEmpty()) {
            StfalconImageViewer.Builder(this, uris) { view, uriString ->
                view.load(uriString.toUri())
            }.show()
        }
    }
}
