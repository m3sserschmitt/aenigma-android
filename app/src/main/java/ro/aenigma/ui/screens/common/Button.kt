package ro.aenigma.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.util.ContextExtensions.copyToClipboard
import ro.aenigma.util.ContextExtensions.shareText

@Composable
fun ShareButton(
    text: String,
    iconTint: Color
) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            context.shareText(text)
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(
                id = R.string.share
            ),
            tint = iconTint
        )
    }
}

@Composable
fun CopyToClipboardButton(
    text: String,
    iconTint: Color
) {
    val context = LocalContext.current
    IconButton(
        onClick = {
            context.copyToClipboard(text)
        }
    ) {
        Icon(
            painter = painterResource(
                id = R.drawable.ic_copy
            ),
            contentDescription = stringResource(
                id = R.string.copy
            ),
            tint = iconTint
        )
    }
}
