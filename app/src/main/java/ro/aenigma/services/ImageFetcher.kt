package ro.aenigma.services

import androidx.compose.ui.graphics.ImageBitmap
import dagger.hilt.android.scopes.ViewModelScoped
import ro.aenigma.data.Repository
import javax.inject.Inject

@ViewModelScoped
open class ImageFetcher @Inject constructor(
    private val repository: Repository?
) {
    open suspend fun fetch(uri: String): ImageBitmap? {
        return repository?.remote?.getImage(uri)
    }
}

class NoOpImageFetcherImpl: ImageFetcher(null) {
    override suspend fun fetch(uri: String): ImageBitmap? {
        return null
    }
}
