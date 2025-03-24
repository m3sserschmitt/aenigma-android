package ro.aenigma.ui.screens.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.data.network.SignalRStatus
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.ConnectionStatusAppBarAction
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.ReplyToMessageAppBarAction
import ro.aenigma.ui.screens.common.RetryConnectionAppBarAction
import ro.aenigma.ui.screens.common.SearchAppBar
import ro.aenigma.ui.screens.common.SelectionModeAppBar
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.RequestState
import java.time.ZonedDateTime

@Composable
fun ChatAppBar(
    messages: RequestState<List<MessageWithDetails>>,
    contact: RequestState<ContactWithGroup>,
    isMember: Boolean,
    connectionStatus: SignalRStatus,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    selectedItemsCount: Int,
    onRetryConnection: () -> Unit,
    onDeleteAllClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onReplyToMessageClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onSelectionModeExited: () -> Unit,
    onSearchModeTriggered: () -> Unit,
    onSearchModeClosed: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onGroupActionClicked: (MessageType) -> Unit,
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
                if (selectedItemsCount == 1) {
                    ReplyToMessageAppBarAction(
                        onReplyToMessageClicked = onReplyToMessageClicked
                    )
                }
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
        } else if(contact is RequestState.Success){
            StandardAppBar(
                title = contact.data.contact.name,
                navigateBack = navigateToContactsScreen,
                actions = {
                    ConnectionStatusAppBarAction(
                        connectionStatus = connectionStatus
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
                        isGroup = contact.data.contact.type == ContactType.GROUP,
                        isMember = isMember,
                        onDeleteAllClicked = onDeleteAllClicked,
                        onRenameContactClicked = onRenameContactClicked,
                        onShareContactClicked = {
                            navigateToAddContactsScreen(contact.data.contact.address)
                        },
                        onGroupActionClicked = onGroupActionClicked
                    )
                }
            )
        }
    }
}

@Composable
fun MoreActions(
    messages: RequestState<List<MessageWithDetails>>,
    isGroup: Boolean,
    isMember: Boolean,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onShareContactClicked: () -> Unit,
    onGroupActionClicked: (MessageType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    BasicDropdownMenu(
        expanded = expanded,
        onToggle = {
            isExpanded -> expanded = isExpanded
        }
    ) {
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(id = R.string.rename),
            text = stringResource(id = R.string.rename),
            visible = (isGroup && isMember) || !isGroup,
            onClick = {
                onRenameContactClicked()
                expanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(id = R.string.share),
            text = stringResource(id = R.string.share),
            visible = !isGroup,
            onClick = {
                onShareContactClicked()
                expanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.add_group_member),
            text = stringResource(id = R.string.add_group_member),
            visible = isGroup && isMember,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_ADD)
                expanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Clear,
            contentDescription = stringResource(id = R.string.remove_group_member),
            text = stringResource(id = R.string.remove_group_member),
            visible = isGroup && isMember,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_REMOVE)
                expanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = stringResource(id = R.string.leave_group),
            text = stringResource(id = R.string.leave_group),
            visible = isGroup && isMember,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_LEFT)
                expanded = false
            }
        )
        if(messages is RequestState.Success)
        {
            BasicDropDownMenuItem(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(id = R.string.delete),
                visible = messages.data.isNotEmpty(),
                text = stringResource(id = R.string.clear_conversation),
                onClick = {
                    onDeleteAllClicked()
                    expanded = false
                }
            )
        }
    }
}

@Composable
@Preview
fun DefaultChatAppBarPreview() {
    ChatAppBar(
        messages = RequestState.Success(listOf()),
        isSelectionMode = false,
        connectionStatus = SignalRStatus.NotConnected(),
        contact = RequestState.Success(
            ContactWithGroup(
                ContactEntity(
                    address = "123456-5678-5678-123456",
                    name = "John",
                    publicKey = "public-key",
                    guardHostname = "guard-hostname",
                    guardAddress = "guard-address",
                    type = ContactType.CONTACT,
                    hasNewMessage = true,
                    lastSynchronized = ZonedDateTime.now()
                ), null
            )
        ),
        isMember = true,
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        onReplyToMessageClicked = {},
        selectedItemsCount = 0,
        onSelectionModeExited = {},
        onSearchModeTriggered = {},
        onGroupActionClicked = {},
        onSearchClicked = {},
        onSearchModeClosed = {},
        isSearchMode = false,
        navigateToAddContactsScreen = {}
    )
}

@Preview
@Composable
fun SelectionModeChatAppBarPreview() {
    ChatAppBar(
        messages = RequestState.Success(listOf()),
        isSelectionMode = true,
        connectionStatus = SignalRStatus.NotConnected(),
        contact = RequestState.Success(
            ContactWithGroup(
                ContactEntity(
                    address = "123456-5678-5678-123456",
                    name = "John",
                    publicKey = "public-key",
                    guardHostname = "guard-hostname",
                    guardAddress = "guard-address",
                    type = ContactType.CONTACT,
                    hasNewMessage = true,
                    lastSynchronized = ZonedDateTime.now()
                ), null
            )
        ),
        isMember = true,
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateToContactsScreen = {},
        onDeleteClicked = {},
        onReplyToMessageClicked = {},
        selectedItemsCount = 3,
        onSelectionModeExited = {},
        onGroupActionClicked = {},
        onSearchModeTriggered = {},
        onSearchClicked = {},
        onSearchModeClosed = {},
        isSearchMode = false,
        navigateToAddContactsScreen = {}
    )
}
