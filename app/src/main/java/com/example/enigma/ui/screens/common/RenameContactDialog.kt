package com.example.enigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R

@Composable
fun RenameContactDialog(
    visible: Boolean,
    onNewContactNameChanged: (String) -> Boolean,
    onConfirmClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    var newContactName by remember { mutableStateOf("") }

    if(visible) {
        EditContactDialog(
            contactName = newContactName,
            onContactNameChanged = {  newValue ->
                newContactName = newValue
                onNewContactNameChanged(newValue)
            },
            title = stringResource(
                id = R.string.rename_contact
            ),
            body = stringResource(
                id = R.string.enter_new_contact_name
            ),
            dismissible = true,
            onConfirmClicked = {
                onConfirmClicked()
                newContactName = ""
            },
            onDismissClicked = {
                onDismiss()
                newContactName = ""
            },
            onDismissRequest = {
                onDismiss()
                newContactName = ""
            }
        )
    }
}

@Preview
@Composable
fun RenameContactDialogPreview()
{
    RenameContactDialog(
        visible = true,
        onNewContactNameChanged = { true },
        onConfirmClicked = {},
        onDismiss = {}
    )
}
