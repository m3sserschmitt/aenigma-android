package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.network.SignalRStatus
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.IndeterminateCircularIndicator
import ro.aenigma.ui.screens.common.RetryConnectionAppBarAction
import ro.aenigma.ui.screens.common.SearchAppBar
import ro.aenigma.ui.screens.common.SelectionModeAppBar
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.DatabaseRequestState
import java.time.ZonedDateTime

@Composable
fun ChatAppBar(
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    connectionStatus: SignalRStatus,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    selectedItemsCount: Int,
    onRetryConnection: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onSelectionModeExited: () -> Unit,
    onSearchModeTriggered: () -> Unit,
    onSearchModeClosed: () -> Unit,
    onSearchClicked: (String) -> Unit,
    navigateToContactsScreen: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(key1 = isSearchMode)
    {
        if (!isSearchMode) {
            searchQuery = ""
        }
    }

    if (isSelectionMode) {
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
        if (isSearchMode) {
            SearchAppBar(
                searchQuery = searchQuery,
                onSearchQueryChanged = { newSearchQuery ->
                    searchQuery = newSearchQuery
                },
                onClose = onSearchModeClosed,
                onSearchClicked = onSearchClicked
            )
        } else {
            StandardAppBar(
                title = if (contact is DatabaseRequestState.Success) contact.data.name else "",
                navigateBack = navigateToContactsScreen,
                actions = {
                    IndeterminateCircularIndicator(
                        modifier = Modifier.size(18.dp),
                        visible = connectionStatus greaterOrEqualThan connectionStatus
                                && connectionStatus smallerThan SignalRStatus.Authenticated(),
                        text = "",
                        fontSize = 12.sp
                    )
                    RetryConnectionAppBarAction(
                        visible = connectionStatus is SignalRStatus.Error.Aborted,
                        onRetryConnection = onRetryConnection
                    )
                    ActivateSearchAppBarAction(
                        onSearchModeTriggered = onSearchModeTriggered
                    )
                    MoreActions(
                        messages = messages,
                        onDeleteAllClicked = onDeleteAllClicked,
                        onRenameContactClicked = onRenameContactClicked,
                        onShareContactClicked = {
                            if (contact is DatabaseRequestState.Success) {
                                navigateToAddContactsScreen(contact.data.address)
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun MoreActions(
    messages: DatabaseRequestState<List<MessageEntity>>,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onShareContactClicked: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    BasicDropdownMenu(
        expanded = expanded,
        onToggle = {
            isExpanded -> expanded = isExpanded
        }
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(
                        id = R.string.rename
                    ),
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.rename
                    )
                )
            },
            onClick = {
                onRenameContactClicked()
                expanded = false
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(
                        id = R.string.share
                    ),
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.share
                    )
                )
            },
            onClick = {
                onShareContactClicked()
                expanded = false
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(
                        id = R.string.delete
                    ),
                )
            },
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
                expanded = false
            }
        )
    }
}

@Composable
@Preview
fun DefaultChatAppBarPreview()
{
    ChatAppBar(
        messages = DatabaseRequestState.Success(listOf()),
        isSelectionMode = false,
        connectionStatus = SignalRStatus.NotConnected(),
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            "guard-address",
            true,
            ZonedDateTime.now()
        )),
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        selectedItemsCount = 0,
        onSelectionModeExited = {},
        onSearchModeTriggered = {},
        onSearchClicked = {},
        onSearchModeClosed = {},
        isSearchMode = false,
        navigateToAddContactsScreen = {}
    )
}

@Preview
@Composable
fun SelectionModeChatAppBarPreview()
{
    ChatAppBar(
        messages = DatabaseRequestState.Success(listOf()),
        isSelectionMode = true,
        connectionStatus = SignalRStatus.NotConnected(),
        contact = DatabaseRequestState.Success(ContactEntity(
            "123456-5678-5678-123456",
            "John",
            "public-key",
            "guard-hostname",
            "guard-address",
            true,
            ZonedDateTime.now()
        )),
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        selectedItemsCount = 3,
        onSelectionModeExited = {},
        onSearchModeTriggered = {},
        onSearchClicked = {},
        onSearchModeClosed = {},
        isSearchMode = false,
        navigateToAddContactsScreen = {}
    )
}
