package com.example.enigma.ui.screens.contacts

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.screens.common.DialogContentTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteSelectedContactsDialog(
    visible: Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if(visible)
    {
        BasicAlertDialog(onDismissRequest = onDismissClicked) {
            DialogContentTemplate(
                title = stringResource(
                    id = R.string.delete_selected_contacts
                ),
                body = stringResource(
                    id = R.string.this_action_is_permanent
                ),
                content = { },
                dismissible = true,
                onConfirmClicked = onConfirmClicked,
                onDismissClicked = onDismissClicked
            )
        }
    }
}

@Preview
@Composable
fun DeleteSelectedContactsDialogPreview()
{
    DeleteSelectedContactsDialog(
        visible = true,
        onConfirmClicked = { },
        onDismissClicked = {}
    )
}
