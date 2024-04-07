package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.DatabaseRequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppBar(
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            BackAction(
                onBackClicked = navigateToContactsScreen
            )
        },
        title = {
            Text(
                text = if(contact is DatabaseRequestState.Success) contact.data.name else "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 24.sp
            )
        },
        actions = {
            MoreActions(
                messages = messages,
                onDeleteAllClicked = onDeleteAllClicked,
                onRenameContactClicked = onRenameContactClicked
            )
        }
    )
}

@Composable
fun BackAction(
    onBackClicked: () -> Unit
) {
    IconButton(
        onClick = { 
            onBackClicked() 
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
        )
    }
}

@Composable
fun MoreActions(
    messages: DatabaseRequestState<List<MessageEntity>>,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit
) {
    var moreActionsDropdownExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = {
                moreActionsDropdownExpanded = !moreActionsDropdownExpanded
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(
                    id = R.string.more_contact_actions
                )
            )
        }
        DropdownMenu(
            expanded = moreActionsDropdownExpanded,
            onDismissRequest = {
                moreActionsDropdownExpanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.rename
                        )
                    )
                },
                onClick = {
                    onRenameContactClicked()
                    moreActionsDropdownExpanded = false
                }
            )
            DropdownMenuItem(
                enabled = messages is DatabaseRequestState.Success && messages.data.isNotEmpty(),
                text = {
                    Text(
                        text = stringResource(
                            id = R.string.clear_conversation
                        )
                    )
                },
                onClick = {
                    onDeleteAllClicked()
                    moreActionsDropdownExpanded = false
                }
            )
        }
    }
}

@Composable
@Preview
fun ChatAppBarPreview()
{
    ChatAppBar(
        messages = DatabaseRequestState.Success(listOf()),
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            true
        )),
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {}
    )
}
