package ro.aenigma.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.normalizeMimeType
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.WorkInfo
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.load
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.stfalcon.imageviewer.StfalconImageViewer
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import okhttp3.OkHttpClient
import org.apache.tika.Tika
import ro.aenigma.R
import ro.aenigma.activities.AppActivity
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.FileDisplayInfoDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.extensions.MessageDtoExtensions.isNotSent
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_CHUNK_PACKING_SIZE
import ro.aenigma.util.Constants.Companion.COIL_MEMORY_CACHE_PERCENTAGE
import ro.aenigma.util.Constants.Companion.IMAGES_CACHE_DIRECTORY
import ro.aenigma.util.Constants.Companion.IMAGE_COMPRESSION_QUALITY
import ro.aenigma.util.Constants.Companion.ORBOT_PACKAGE
import ro.aenigma.util.Constants.Companion.ORBOT_STORE_LINK
import ro.aenigma.util.Constants.Companion.ORBOT_WEB_LINK
import ro.aenigma.util.Constants.Companion.PRIVATE_KEY_FILE
import ro.aenigma.util.Constants.Companion.PUBLIC_KEY_FILE
import ro.aenigma.util.Constants.Companion.WEB_ARTICLE_URL_TEMPLATE
import ro.aenigma.util.ContentResolverExtensions.getExtension
import ro.aenigma.util.ContentResolverExtensions.querySize
import ro.aenigma.util.FileExtensions.lengthSafe
import ro.aenigma.util.StringExtensions.isApkMime
import ro.aenigma.util.StringExtensions.isArchiveMime
import ro.aenigma.util.StringExtensions.isAudioMime
import ro.aenigma.util.StringExtensions.isImageMime
import ro.aenigma.util.StringExtensions.isJsonMime
import ro.aenigma.util.StringExtensions.isMarkdownMime
import ro.aenigma.util.StringExtensions.isPdfMime
import ro.aenigma.util.StringExtensions.isRemoteUri
import ro.aenigma.util.StringExtensions.isTextMime
import ro.aenigma.util.StringExtensions.isVideoMime
import ro.aenigma.util.UriExtensions.isRemote
import java.io.BufferedInputStream
import java.io.File
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(Constants.DATASTORE_PREFERENCES)

object ContextExtensions {

    fun Context.createImageLoader(client: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(this).memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(this, COIL_MEMORY_CACHE_PERCENTAGE)
                .build()
        }.diskCache {
            DiskCache.Builder()
                .directory(getImagesCacheDirectory())
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

    suspend fun Context.getFileType(uri: Uri): String? {
        val resolver = contentResolver
        return withContext(Dispatchers.IO) {
            try {
                normalizeMimeType(
                    resolver.getType(uri) ?: getFileTypeByExtension(uri)
                    ?: getFileTypeByParsing(uri)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    fun Context.getFileTypeByParsing(uri: Uri): String? {
        return contentResolver.openInputStream(uri)?.use { input ->
            BufferedInputStream(input).use { buffered ->
                Tika().detect(buffered)
            }
        }
    }

    fun Context.getFileTypeByExtension(uri: Uri): String? {
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(getFileExtension(uri)?.lowercase() ?: return null)
    }

    fun Context.getFileExtension(uri: Uri): String? {
        return contentResolver.getExtension(uri)
    }

    suspend fun Context.getFileType(uri: String): String? {
        return getFileType(uri.toUri())
    }

    suspend fun Context.getFileTypeIcon(uri: String): FileDisplayInfoDto {
        if (uri.isRemoteUri()) {
            return FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_link,
                isImage = false
            )
        }
        val mime = getFileType(uri)
        return when {
            mime.isImageMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_photo,
                isImage = true
            )

            mime.isVideoMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_video_file
            )

            mime.isAudioMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_audio_file
            )

            mime.isPdfMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_pdf
            )

            mime.isApkMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_apk_file
            )

            mime.isArchiveMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_zip
            )

            mime.isMarkdownMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_markdown
            )

            mime.isJsonMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_json
            )

            mime.isTextMime() -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_text
            )

            else -> FileDisplayInfoDto(
                painterResourceId = R.drawable.ic_unknown_document
            )
        }
    }

    fun Context.getPrivateKeyFile(): File {
        return File(filesDir, PRIVATE_KEY_FILE)
    }

    fun Context.getPublicKeyFile(): File {
        return File(filesDir, PUBLIC_KEY_FILE)
    }

    private suspend fun createDirectory(parent: File, directory: String): File {
        return withContext(Dispatchers.IO) {
            val file = File(parent, directory)
            if (!file.exists()) {
                file.mkdirs()
            }
            file
        }
    }

    suspend fun Context.createAppFilesDirectory(directory: String): File {
        return createDirectory(filesDir, directory)
    }

    suspend fun Context.createTempCacheDirectory(): File {
        return createDirectory(cacheDir, "${UUID.randomUUID()}")
    }

    fun Context.getImagesCacheDirectory(): File {
        return File(cacheDir, IMAGES_CACHE_DIRECTORY)
    }

    private suspend fun createTempCacheFile(directory: File, suffix: String?): File {
        return withContext(Dispatchers.IO) {
            File.createTempFile("${UUID.randomUUID()}", suffix, directory)
        }
    }

    suspend fun Context.createTempCacheFile(suffix: String?): File {
        return createTempCacheFile(cacheDir, suffix)
    }

    fun Context.getCacheFile(relativePath: String): File {
        return File(cacheDir, relativePath)
    }

    suspend fun Context.getAppFile(directory: String, suffix: String?): File {
        return File(
            createAppFilesDirectory(directory),
            "${UUID.randomUUID()}${
                if (suffix.isNullOrBlank()) {
                    ""
                } else {
                    suffix
                }
            }"
        )
    }

    fun Context.toContentUri(file: File): Uri {
        return FileProvider.getUriForFile(this, "${this.packageName}.fileprovider", file)
    }

    suspend fun Context.readTextFromUri(uri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    input.bufferedReader().use { bufferedReader -> bufferedReader.readText() }
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun Context.readTextFromUri(uri: String): String? {
        return readTextFromUri(uri.toUri())
    }

    suspend fun Context.extractZip(zipFile: File, directory: String): List<File> {
        return withContext(Dispatchers.IO) {
            val tempDir = createTempCacheDirectory()
            try {
                ZipFile(zipFile).extractAll(tempDir.absolutePath)

                val finalFiles = mutableListOf<File>()
                tempDir.walkTopDown().filter { item -> item.isFile }.forEach { extractedFile ->
                    val destFile = getAppFile(directory, ".${extractedFile.extension}")
                    extractedFile.copyTo(destFile, overwrite = true)
                    extractedFile.delete()
                    finalFiles.add(destFile)
                }
                finalFiles
            } catch (_: Exception) {
                listOf()
            } finally {
                tempDir.deleteRecursively()
            }
        }
    }

    suspend fun Context.createZip(
        uris: List<String>
    ): File? {
        return withContext(Dispatchers.IO) {
            val tempDir = createTempCacheDirectory()
            val tempFiles = copyUris(uris, tempDir)
            if (!tempFiles.isEmpty()) {
                try {
                    val zipParameters = ZipParameters().apply {
                        compressionLevel = CompressionLevel.MAXIMUM
                        compressionMethod = CompressionMethod.DEFLATE
                    }

                    val zipFile = createTempCacheFile(null)
                    val archive = ZipFile(zipFile)

                    tempFiles.forEach { file ->
                        archive.addFile(file, zipParameters)
                    }
                    zipFile
                } catch (_: Exception) {
                    null
                } finally {
                    tempDir.deleteRecursively()
                }
            } else {
                null
            }
        }
    }

    suspend fun Context.copyUris(uris: List<String>, directory: File): List<File> {
        return withContext(Dispatchers.IO) {
            val finalFiles = mutableListOf<File>()
            try {
                uris.forEach { uri ->
                    val parsedUri = uri.toUri()
                    val file = createTempCacheFile(directory, ".${getFileExtension(parsedUri)}")
                    file.outputStream().use { outputStream ->
                        contentResolver.openInputStream(parsedUri)?.use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    if (isImageUri(uri)) {
                        val compressedFile = compressImage(file)
                        file.delete()
                        compressedFile.renameTo(file)
                    }
                    finalFiles.add(file)
                }
                finalFiles
            } catch (_: Exception) {
                finalFiles
            }
        }
    }

    fun Context.deleteUri(uri: Uri): Boolean {
        return try {
            contentResolver.delete(uri, null, null) > 0
        } catch (_: Exception) {
            false
        }
    }

    fun Context.deleteUri(uri: String): Boolean {
        return deleteUri(uri.toUri())
    }

    suspend fun Context.isImageUri(uri: String): Boolean {
        return uri.isNotBlank() && isImageUri(uri.toUri())
    }

    suspend fun Context.isImageUri(uri: Uri): Boolean {
        return getFileType(uri).isImageMime()
    }

    suspend fun Context.isMarkdownUri(uri: Uri): Boolean {
        return getFileType(uri).isMarkdownMime()
    }

    suspend fun Context.isMarkdownUri(uri: String): Boolean {
        return isMarkdownUri(uri.toUri())
    }

    suspend fun Context.isJsonUri(uri: Uri): Boolean {
        return getFileType(uri).isJsonMime()
    }

    suspend fun Context.isJsonUri(uri: String): Boolean {
        return isJsonUri(uri.toUri())
    }

    suspend fun Context.isTextUri(uri: Uri): Boolean {
        return getFileType(uri).isTextMime()
    }

    suspend fun Context.isTextUri(uri: String): Boolean {
        return isTextUri(uri.toUri())
    }

    private fun Context.sizeOf(uri: String): Long {
        val parsedUri = uri.toUri()

        return when {
            parsedUri.scheme?.equals(ContentResolver.SCHEME_CONTENT, true) == true ->
                contentResolver.querySize(parsedUri)

            parsedUri.scheme?.equals(ContentResolver.SCHEME_FILE, true) == true ->
                File(parsedUri.path ?: return -1).lengthSafe()

            parsedUri.scheme == null -> File(uri).lengthSafe()

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

    suspend fun Context.openUriInExternalApp(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = uri
                addCategory(Intent.CATEGORY_BROWSABLE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (uri.scheme == "content") {
                    val type = getFileType(uri)
                    if (!type.isNullOrBlank()) {
                        setDataAndType(uri, type)
                    }
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
                    getString(R.string.share_via)
                )
            )
        } catch (_: Exception) {
            Toast.makeText(
                this,
                getString(R.string.failed_to_share),
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

    suspend fun Context.shareUri(uri: Uri) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = getFileType(uri)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(
                Intent.createChooser(intent, getString(R.string.share_via))
            )
        } catch (_: Exception) {
            Toast.makeText(
                this,
                getString(R.string.failed_to_share),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    suspend fun Context.shareUriOrText(uri: Uri) {
        if (uri.isRemote()) {
            shareText(text = uri.toString())
        } else {
            shareUri(uri = uri)
        }
    }

    suspend fun Context.shareArticleUriOrText(uri: Uri) {
        if (uri.isRemote()) {
            shareText(String.format(WEB_ARTICLE_URL_TEMPLATE, Uri.encode(uri.toString())))
        } else {
            shareUri(uri)
        }
    }

    suspend fun Context.shareArticleUriOrText(uri: String) {
        shareArticleUriOrText(uri.toUri())
    }

    suspend fun Context.showImageViewer(
        uris: List<String>,
        onDismiss: () -> Unit = { }
    ) {
        val filteredUris = uris.filter { item -> isImageUri(item) && !item.isRemoteUri() }
        if (filteredUris.isNotEmpty()) {
            StfalconImageViewer.Builder(this, filteredUris) { view, uriString ->
                view.load(uriString.toUri())
            }.withDismissListener {
                onDismiss.invoke()
            }.show()
        }
    }

    suspend fun Context.showImageViewer(
        message: MessageDto,
        onDismiss: () -> Unit = { }
    ) {
        if (message.isNotSent() && message.deliveryStatus.value == WorkInfo.State.FAILED) {
            return
        }
        showImageViewer(
            uris = if (message.files.isNullOrEmpty()) {
                message.filesLate.value
            } else {
                message.files
            },
            onDismiss = onDismiss
        )
    }

    suspend fun Context.showImageViewer(
        article: ArticleDto,
        onDismiss: () -> Unit = { }
    ) {
        if (!article.url.isNullOrBlank()) {
            return
        }
        showImageViewer(
            uris = article.imageUrls?.mapNotNull { uri -> uri } ?: listOf(),
            onDismiss = onDismiss
        )
    }

    suspend fun Context.compressImage(image: File): File {
        return Compressor.compress(this, image, Dispatchers.IO) {
            quality(IMAGE_COMPRESSION_QUALITY)
            format(Bitmap.CompressFormat.JPEG)
        }
    }

    fun Context.isOrbotInstalled(): Boolean {
        try {
            packageManager.getPackageInfo(ORBOT_PACKAGE, PackageManager.GET_ACTIVITIES)
            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun Context.redirectToOrbotOnPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, ORBOT_STORE_LINK.toUri()))
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, ORBOT_WEB_LINK.toUri()))
        }
    }

    fun Context.openOrbot() {
        val launchIntent = packageManager.getLaunchIntentForPackage(ORBOT_PACKAGE)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            redirectToOrbotOnPlayStore()
        }
    }
}
