package ro.aenigma.ui.screens.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import ro.aenigma.R
import ro.aenigma.services.ImageFetcher
import ro.aenigma.services.NoOpImageFetcherImpl
import ro.aenigma.util.ContextExtensions.getBitmapFromDrawable
import ro.aenigma.util.isRemoteUri

@Composable
fun RemoteSecureAsyncImage(
    uri: String?,
    imageFetcher: ImageFetcher,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val placeholder = remember {
        context.getBitmapFromDrawable(R.drawable.ic_outline_broken_image)?.asImageBitmap()
    }
    val bitmap by produceState(initialValue = placeholder, key1 = uri) {
        value = try {
            if (uri != null) {
                imageFetcher.fetch(uri)
            } else {
                placeholder
            }
        } catch (_: Exception) {
            placeholder
        }
    }
    bitmap?.let {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .padding(bottom = 12.dp),
            bitmap = it,
            contentDescription = stringResource(id = R.string.files),
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
                .placeholder(R.drawable.ic_outline_broken_image)
                .error(R.drawable.ic_outline_broken_image)
                .build(),
            contentDescription = stringResource(id = R.string.files),
            contentScale = ContentScale.Fit,
        )
    }
}
