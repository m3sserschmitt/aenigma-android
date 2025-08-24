package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.UrlExtensions.isRemoteUri
import ro.aenigma.util.rememberImageLoader

@Composable
private fun RemoteAsyncImage(
    uri: String,
    okHttpClientProvider: IOkHttpClientProvider,
    contentScale: ContentScale = ContentScale.Fit
) {
    val imageLoader = rememberImageLoader(okHttpClientProvider, uri)
    if(imageLoader != null) {
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
            contentScale = contentScale,
            imageLoader = imageLoader
        )
    }
}

@Composable
fun AsyncImage(
    uri: String,
    okHttpClientProvider: IOkHttpClientProvider,
    contentScale: ContentScale = ContentScale.Fit
) {
    val isRemote = remember(key1 = uri) { uri.isRemoteUri() }
    if (isRemote) {
        RemoteAsyncImage(
            uri = uri,
            okHttpClientProvider = okHttpClientProvider,
            contentScale = contentScale
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
