package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.ui.screens.common.TextInputDialog

@Composable
fun SetupNameDialog(
    visible: Boolean,
    onConfirmClicked: (String) -> Unit
) {
    if (visible) {
        var name by remember { mutableStateOf("") }
        TextInputDialog(
            title = stringResource(
                id = R.string.whats_your_name
            ),
            body = stringResource(
                id = R.string.name_dialog_body
            ),
            onTextChanged = {
                newValue -> name = newValue
                true
            },
            onConfirmClicked = {
                onConfirmClicked(name)
            },
            onDismissClicked = { },
            dismissible = false,
            placeholderText = ""
        )
    }
}
