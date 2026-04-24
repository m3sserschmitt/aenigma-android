package ro.aenigma.ui.screens.common

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import kotlinx.coroutines.launch
import ro.aenigma.R
import ro.aenigma.util.ContextExtensions.copyToClipboard
import ro.aenigma.util.ContextExtensions.openUriInExternalApp
import ro.aenigma.util.ContextExtensions.shareText
import ro.aenigma.util.ContextExtensions.shareUriOrText

@Composable
fun ShareButton(
    tint: Color = Color.Unspecified,
    onClick: () -> Unit = { }
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(id = R.string.share),
            tint = tint
        )
    }
}

@Composable
fun OpenInExternalAppButton(
    tint: Color = Color.Unspecified,
    onClick: () -> Unit = { }
) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_open),
            contentDescription = stringResource(id = R.string.open),
            tint = tint
        )
    }
}

@Composable
fun RedirectUriButton(
    tint: Color = Color.Unspecified,
    onClick: () -> Unit = { }
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_forward),
            contentDescription = stringResource(id = R.string.forward),
            tint = tint
        )
    }
}

@Composable
fun OpenInExternalAppButton(
    uri: Uri,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    OpenInExternalAppButton(
        tint = tint,
        onClick = { coroutineScope.launch { context.openUriInExternalApp(uri) } }
    )
}

@Composable
fun OpenInExternalAppButton(
    uri: String,
    tint: Color = Color.Unspecified,
) {
    OpenInExternalAppButton(
        tint = tint,
        uri = uri.toUri()
    )
}

@Composable
fun ShareUriButton(
    uri: String,
    tint: Color = Color.Unspecified
) {
    ShareUriButton(
        uri = uri.toUri(),
        tint = tint
    )
}

@Composable
fun ShareUriButton(
    uri: Uri,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    ShareButton(
        tint = tint,
        onClick = { coroutineScope.launch { context.shareUriOrText(uri) } }
    )
}

@Composable
fun ShareTextButton(
    text: String,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    ShareButton(
        tint = tint,
        onClick = { context.shareText(text) }
    )
}

@Composable
fun CopyToClipboardButton(
    text: String,
    tint: Color = Color.Unspecified
) {
    val context = LocalContext.current
    IconButton(
        onClick = { context.copyToClipboard(text) }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = stringResource(id = R.string.copy),
            tint = tint
        )
    }
}

@Composable
fun SendButton(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit = { }
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = stringResource(
                id = R.string.send
            ),
            tint = tint
        )
    }
}

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = { }
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        onClick = onClick
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
