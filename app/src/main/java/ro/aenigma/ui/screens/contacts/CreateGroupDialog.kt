package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.ui.screens.common.TextInputDialog

@Composable
fun CreateGroupDialog(
    visible: Boolean,
    onTextChanged: (String) -> Boolean,
    onConfirmClicked: (String) -> Unit,
    onDismissClicked: () -> Unit
) {
    if (visible) {
        TextInputDialog(
            title = stringResource(
                id = R.string.create_channel
            ),
            body = stringResource(
                id = R.string.enter_channel_name
            ),
            onTextChanged = onTextChanged,
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = onDismissClicked,
            placeholderText = ""
        )
    }
}

@Preview
@Composable
fun CreateGroupDialogPreview() {
    CreateGroupDialog(
        visible = true,
        onTextChanged = { true },
        onDismissClicked = { },
        onConfirmClicked = { }
    )
}
