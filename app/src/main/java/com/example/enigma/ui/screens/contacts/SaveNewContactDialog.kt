package com.example.enigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.screens.common.EditContactDialog

@Composable
fun SaveNewContactDialog(
    visible: Boolean,
    onContactNameChanged: (String) -> Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if(visible) {
        EditContactDialog(
            onContactNameChanged = onContactNameChanged,
            title = stringResource(
                id = R.string.request_successfully_completed
            ),
            body = stringResource(
                id = R.string.save_contact_no_return
            ),
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
