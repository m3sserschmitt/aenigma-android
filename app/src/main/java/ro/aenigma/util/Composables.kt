package ro.aenigma.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import okhttp3.OkHttpClient
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.ContextExtensions.createImageLoader

@Composable
fun rememberImageLoader(clientProvider: IOkHttpClientProvider, link: String): ImageLoader? {
    val context = LocalContext.current
    val client by produceState<OkHttpClient?>(
        initialValue = null,
        key1 = clientProvider,
        key2 = link
    ) {
        value = clientProvider.getInstance()
    }
    return remember(client, context) {
        client?.let { client -> context.createImageLoader(client) }
    }
}
