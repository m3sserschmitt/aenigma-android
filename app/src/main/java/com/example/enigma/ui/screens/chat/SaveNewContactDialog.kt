package com.example.enigma.ui.screens.chat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.ui.screens.common.EditContactDialog
import com.example.enigma.util.DatabaseRequestState

@Composable
fun SaveNewContactDialog(
    contact: DatabaseRequestState<ContactEntity>,
    onNewContactNameChanged: (String) -> Boolean,
    onNewNameConfirmClicked: () -> Unit
) {
    if (contact is DatabaseRequestState.Success && contact.data.name.isEmpty())
    {
        EditContactDialog(
            onContactNameChanged = onNewContactNameChanged,
            title = stringResource(
                id = R.string.new_contact_available
            ),
            body = stringResource(
                id = R.string.provide_new_name
            ),
            dismissible = false,
            onConfirmClicked = onNewNameConfirmClicked,
            onDismissClicked = { },
            onDismissRequest = { }
        )
    }
}

@Preview
@Composable
fun SaveNewContactDialogPreview()
{
    SaveNewContactDialog(
        contact = DatabaseRequestState.Success(
            ContactEntity(
                address = "123",
                name = "",
                publicKey = "key",
                guardHostname = "guard",
                hasNewMessage = false
            )
        ),
        onNewContactNameChanged = { true },
        onNewNameConfirmClicked = {}
    )
}
