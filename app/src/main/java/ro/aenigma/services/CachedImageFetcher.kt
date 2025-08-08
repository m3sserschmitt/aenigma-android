package ro.aenigma.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ro.aenigma.data.Repository
import ro.aenigma.util.Constants.Companion.IMAGES_CACHE_DIR
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject

@ViewModelScoped
class CachedImageFetcher @Inject constructor(
    private val delegate: ImageFetcher,
    @ApplicationContext context: Context,
    repository: Repository
) : ImageFetcher(repository) {

    private val cacheDir = File(context.cacheDir, IMAGES_CACHE_DIR).apply { mkdirs() }

    override suspend fun fetch(uri: String): ImageBitmap? {
        val key = uri.toCacheKey()
        val file = File(cacheDir, key)

        if (file.exists()) {
            return try {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } catch (_: Exception) {
                try {
                    file.delete()
                } catch (_: Exception) {
                }
                delegate.fetch(uri)
            }
        }

        val image = delegate.fetch(uri) ?: return null

        saveToDisk(image, file)
        return image
    }

    private fun saveToDisk(image: ImageBitmap, file: File) {
        try {
            file.parentFile?.mkdirs()
            val bitmap = image.asAndroidBitmap()
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
            }
        } catch (_: Exception) {
        }
    }

    private fun String.toCacheKey(): String {
        return URLEncoder.encode(this, Charsets.UTF_8.name())
    }
}
