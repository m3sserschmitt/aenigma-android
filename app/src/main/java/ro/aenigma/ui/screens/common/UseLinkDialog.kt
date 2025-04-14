package ro.aenigma.ui.screens.common

import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@Composable
fun UseLinkDialog(
    visible: Boolean,
    onConfirmClicked: (String) -> Unit,
    onDismissClicked: () -> Unit
) {
    if (visible) {
        TextInputDialog(
            onTextChanged = { link ->
                Patterns.WEB_URL.matcher(link).matches()
            },
            title = stringResource(
                id = R.string.enter_link
            ),
            body = stringResource(
                id = R.string.enter_link_to_get_contact
            ),
            placeholderText = stringResource(id = R.string.link),
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = onDismissClicked,
        )
    }
}

@Preview
@Composable
fun UseLinkDialogPreview()
{
    UseLinkDialog(
        visible = true,
        onConfirmClicked = { },
        onDismissClicked = { }
    )
}
