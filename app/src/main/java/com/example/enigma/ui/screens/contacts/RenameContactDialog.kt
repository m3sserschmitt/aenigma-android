package com.example.enigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.util.DatabaseRequestState

@Composable
fun RenameContactDialog (
    visible: Boolean,
    contacts: DatabaseRequestState<List<ContactEntity>>,
    onContactRenamed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    com.example.enigma.ui.screens.common.RenameContactDialog(
        visible = visible,
        newContactName = name,
        onNewContactNameChanged = { newValue ->
            if (contacts is DatabaseRequestState.Success) {
                name = newValue
                !contacts.data.any { item -> item.name == newValue }
            } else {
                false
            }
        },
        onConfirmClicked = {
            onContactRenamed(name)
            name = ""
        },
        onDismiss = {
            name = ""
            onDismiss()
        }
    )
}

@Preview
@Composable
fun RenameContactDialogPreview()
{
    RenameContactDialog(
        visible = true,
        contacts = DatabaseRequestState.Success(listOf()),
        onContactRenamed = {},
        onDismiss = {}
    )
}
