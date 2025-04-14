package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.ui.screens.common.TextInputDialog

@Composable
fun SetupUserNameDialog(
    visible: Boolean,
    onConfirmClicked: (String) -> Unit
) {
    if (visible) {
        TextInputDialog(
            title = stringResource(
                id = R.string.whats_your_name
            ),
            body = stringResource(
                id = R.string.name_dialog_body
            ),
            onTextChanged = { true },
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = { },
            dismissible = false,
            placeholderText = ""
        )
    }
}
