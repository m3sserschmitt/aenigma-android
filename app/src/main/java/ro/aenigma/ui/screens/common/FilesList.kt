package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ro.aenigma.R
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.util.ContextExtensions.getFileTypeIcon
import ro.aenigma.util.ContextExtensions.isImageUri
import ro.aenigma.util.ContextExtensions.openUriInExternalApp
import ro.aenigma.util.UrlExtensions.isImageUrlByExtension
import ro.aenigma.util.UrlExtensions.isRemoteUri

@Composable
fun FilesList(
    uris: List<String>,
    textColor: Color = Color.Unspecified,
    okHttpClientProvider: IOkHttpClientProvider
) {
    if(uris.isNotEmpty()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            uris.forEach { uri ->
                FileItem(
                    uri = uri,
                    okHttpClientProvider = okHttpClientProvider
                )
            }
        }
    } else {
        NoFilesWarning(
            textColor = textColor
        )
    }
}

@Composable
fun NoFilesWarning(
    textColor: Color = Color.Unspecified
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .size(18.dp)
                .alpha(0.75f),
            imageVector = Icons.Outlined.Warning,
            contentDescription = stringResource(R.string.no_files_available),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier =Modifier.width(4.dp))
        Text(
            text = stringResource(id = R.string.no_files_available),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = textColor
        )
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
fun getUriTitle(uri: String): String {
    val parsedUri = uri.toUri()
    return if (uri.isRemoteUri()) {
        parsedUri.host
    } else {
        parsedUri.lastPathSegment
    } ?: stringResource(id = R.string.unknown_file)
}

@Composable
fun FileItem(
    uri: String,
    okHttpClientProvider: IOkHttpClientProvider
) {
    val isImage by rememberIsImage(uri)
    if (isImage == true) {
        AsyncImage(
            uri = uri,
            okHttpClientProvider = okHttpClientProvider
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val context = LocalContext.current
                Icon(
                    modifier = Modifier.alpha(.75f).size(36.dp),
                    painter = painterResource(context.getFileTypeIcon(uri)),
                    contentDescription = stringResource(R.string.files),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = getUriTitle(uri),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(
                    onClick = { context.openUriInExternalApp(uri.toUri()) },
                ) {
                    Icon(
                        modifier = Modifier.alpha(.75f).size(24.dp),
                        painter = painterResource(R.drawable.ic_open),
                        contentDescription = stringResource(id = R.string.open),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
