package ro.aenigma.services

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.aenigma.R
import javax.inject.Inject

@ViewModelScoped
class MarkdownImageTransformer @Inject constructor(
    private val imageFetcher: CachedImageFetcher
): ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData? {
        val bitmapState by produceState<ImageBitmap?>(null, link) {
            value = withContext(Dispatchers.IO) { imageFetcher.fetch(link) }
        }

        val painter: Painter = when (val bmp = bitmapState) {
            null -> painterResource(R.drawable.ic_broken_image)
            else -> remember(bmp) { BitmapPainter(bmp) }
        }

        return ImageData(
            painter = painter,
            contentDescription = null,
            alignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxWidth()
        )
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size =
        painter.intrinsicSize
}
