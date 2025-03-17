package ro.aenigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@Composable
fun SaveNewContactDialog(
    visible: Boolean,
    onContactNameChanged: (String) -> Boolean,
    onConfirmClicked: (String) -> Unit,
    onDismissClicked: () -> Unit
) {
    if(visible) {
        TextInputDialog(
            onTextChanged = onContactNameChanged,
            title = stringResource(
                id = R.string.contact_details_successfully_retrieved
            ),
            body = stringResource(
                id = R.string.save_contact_message
            ),
            placeholderText = stringResource(id = R.string.contact_name),
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = onDismissClicked
        )
    }
}

@Preview
@Composable
fun SaveNewContactDialogPreview()
{
    SaveNewContactDialog(
        visible = true,
        onContactNameChanged = { true },
        onConfirmClicked = { },
        onDismissClicked = { },
    )
}
