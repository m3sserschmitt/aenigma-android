package ro.aenigma.services

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import dagger.hilt.android.scopes.ViewModelScoped
import ro.aenigma.R
import ro.aenigma.util.rememberImageLoader
import javax.inject.Inject

@ViewModelScoped
class MarkdownImageTransformer @Inject constructor(
    private val okHttpClientProvider: OkHttpClientProvider
): ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData? {
        val imageLoader = rememberImageLoader(okHttpClientProvider, link)
        val painter = if (imageLoader == null) {
            painterResource(R.drawable.ic_broken_image)
        } else {
            val context = LocalContext.current
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(link)
                    .crossfade(true)
                    .error(R.drawable.ic_broken_image)
                    .build(),
                imageLoader = imageLoader
            )
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
