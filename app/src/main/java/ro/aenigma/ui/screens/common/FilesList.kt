package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.aenigma.R
import ro.aenigma.services.ImageFetcher
import ro.aenigma.util.ContextExtensions.isImageUri
import ro.aenigma.util.ContextExtensions.openUriInExternalApp
import ro.aenigma.util.UrlExtensions.isImageUrlByExtension

@Composable
fun FilesList(
    uris: List<String>,
    imageFetcher: ImageFetcher
) {
    Column {
        uris.forEach { uri ->
            FileItem(
                uri = uri,
                imageFetcher = imageFetcher
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun rememberIsImage(uri: String): State<Boolean?> {
    val context = LocalContext.current
    return produceState(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            context.isImageUri(uri) || uri.isImageUrlByExtension()
        }
    }
}

@Composable
fun FileItem(
    uri: String,
    imageFetcher: ImageFetcher
) {
    val isImage by rememberIsImage(uri)
    if (isImage == true) {
        SecureAsyncImage(
            uri = uri,
            imageFetcher = imageFetcher
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val parsedUri = uri.toUri()
                Text(
                    text = parsedUri.lastPathSegment ?: stringResource(id = R.string.unknown_file),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val context = LocalContext.current
                Button(onClick = { context.openUriInExternalApp(parsedUri) }) {
                    Text(
                        text = stringResource(id = R.string.open)
                    )
                }
            }
        }
    }
}
