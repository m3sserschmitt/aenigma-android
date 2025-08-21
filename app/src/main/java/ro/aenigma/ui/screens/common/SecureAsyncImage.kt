package ro.aenigma.ui.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.aenigma.R
import ro.aenigma.services.ImageFetcher
import ro.aenigma.services.NoOpImageFetcherImpl
import ro.aenigma.util.ContextExtensions.getBitmapFromDrawable
import ro.aenigma.util.UrlExtensions.isRemoteUri

@Composable
fun produceImageBitmap(
    uri: String?,
    imageFetcher: ImageFetcher
): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState(
        initialValue = null,
        key1 = uri
    ) {
        value = withContext(Dispatchers.IO) {
            if (!uri.isNullOrBlank()) {
                imageFetcher.fetch(uri) ?: context.getBitmapFromDrawable(R.drawable.ic_broken_image)
            } else {
                context.getBitmapFromDrawable(R.drawable.ic_image)
            }
        }
    }
}

@Composable
private fun RemoteSecureAsyncImage(
    uri: String?,
    imageFetcher: ImageFetcher,
    contentScale: ContentScale = ContentScale.Fit
) {
    val bitmap by produceImageBitmap(
        uri = uri,
        imageFetcher = imageFetcher
    )
    bitmap?.let { bitmapToShow ->
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 12.dp),
            bitmap = bitmapToShow,
            contentDescription = stringResource(id = R.string.picture),
            contentScale = contentScale
        )
    }
}

@Composable
fun SecureAsyncImage(
    uri: String?,
    imageFetcher: ImageFetcher = NoOpImageFetcherImpl()
) {
    val isRemote = remember(key1 = uri) { uri.isRemoteUri() }
    if (isRemote) {
        RemoteSecureAsyncImage(
            uri = uri,
            imageFetcher = imageFetcher
        )
    } else {
        val context = LocalContext.current
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 12.dp),
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .placeholder(R.drawable.ic_image)
                .error(R.drawable.ic_broken_image)
                .build(),
            contentDescription = stringResource(id = R.string.picture),
            contentScale = ContentScale.Fit
        )
    }
}
