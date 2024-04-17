package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
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
import com.example.enigma.ui.screens.common.DeleteAppBarAction
import com.example.enigma.ui.screens.common.NavigateBackAppBarAction
import com.example.enigma.ui.screens.common.SelectionModeAppBar
import com.example.enigma.util.DatabaseRequestState

@Composable
fun ChatAppBar(
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    isSelectionMode: Boolean,
    selectedItemsCount: Int,
    onDeleteAllClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onSelectionModeExited: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    if(isSelectionMode)
    {
        SelectionModeAppBar(
            selectedItemsCount = selectedItemsCount,
            onSelectionModeExited = onSelectionModeExited,
            actions = {
                DeleteAppBarAction(
                    onDeleteClicked = onDeleteClicked
                )
            }
        )
    } else {
        DefaultChatAppBar(
            messages = messages,
            contact = contact,
            onDeleteAllClicked = onDeleteAllClicked,
            onRenameContactClicked = onRenameContactClicked,
            navigateToContactsScreen = navigateToContactsScreen
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultChatAppBar(
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            NavigateBackAppBarAction(
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
fun DefaultChatAppBarPreview()
{
    ChatAppBar(
        messages = DatabaseRequestState.Success(listOf()),
        isSelectionMode = false,
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            true
        )),
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        selectedItemsCount = 0,
        onSelectionModeExited = {}
    )
}

@Preview
@Composable
fun SelectionModeChatAppBarPreview()
{
    ChatAppBar(
        messages = DatabaseRequestState.Success(listOf()),
        isSelectionMode = true,
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            true
        )),
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        selectedItemsCount = 3,
        onSelectionModeExited = {}
    )
}
