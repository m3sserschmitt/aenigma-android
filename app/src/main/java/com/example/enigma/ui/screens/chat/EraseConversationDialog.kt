package com.example.enigma.ui.screens.chat

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.screens.common.DialogContentTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EraseConversationDialog(
    visible: Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if (visible ) {
        BasicAlertDialog(onDismissRequest = onDismissClicked) {
            DialogContentTemplate(
                title = stringResource(id = R.string.erase_entire_conversation),
                body = stringResource(id = R.string.this_action_is_permanent),
                dismissible = true,
                onDismissClicked = onDismissClicked,
                onConfirmClicked = onConfirmClicked,
                content = { }
            )
        }
    }
}

@Preview
@Composable
fun EraseConversationDialogPreview()
{
    EraseConversationDialog(
        visible = true,
        onDismissClicked = {},
        onConfirmClicked = {}
    )
}
