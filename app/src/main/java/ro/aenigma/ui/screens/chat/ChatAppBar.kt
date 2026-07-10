/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.ui.screens.chat

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.services.ClientStatus
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.ReplyToMessageAppBarAction
import ro.aenigma.ui.screens.common.ReloadClientAppBarAction
import ro.aenigma.ui.screens.common.SearchAppBar
import ro.aenigma.ui.screens.common.SelectionModeAppBar
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.RequestState

@Composable
fun ChatAppBar(
    messages: RequestState<List<MessageWithDetailsDto>>,
    contact: RequestState<ContactWithGroupDto>,
    isMember: Boolean,
    isAdmin: Boolean,
    moreOptionsMenuExpanded: Boolean = false,
    connectionStatus: ClientStatus,
    isClientWorkerRunning: Boolean = false,
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
    navigateBack: () -> Unit,
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        onReplyToMessageClicked = onReplyToMessageClicked
                    )
                }
                DeleteAppBarAction(
                    onDeleteClicked = onDeleteClicked,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
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
                title = contact.data.contact.name.toString(),
                navigateBack = navigateBack,
                actions = {
                    ReloadClientAppBarAction(
                        isClientWorkerRunning = isClientWorkerRunning,
                        connectionStatus = connectionStatus,
                        tint = MaterialTheme.colorScheme.onBackground,
                        onClick = onRetryConnection
                    )
                    ActivateSearchAppBarAction(
                        tint = MaterialTheme.colorScheme.onBackground,
                        onSearchModeTriggered = onSearchModeTriggered
                    )
                    MoreActions(
                        messages = messages,
                        isGroup = contact.data.contact.type == ContactType.GROUP,
                        isMember = isMember,
                        isAdmin = isAdmin,
                        expanded = moreOptionsMenuExpanded,
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
    messages: RequestState<List<MessageWithDetailsDto>>,
    isGroup: Boolean,
    isMember: Boolean,
    isAdmin: Boolean,
    expanded: Boolean = false,
    onDeleteAllClicked: () -> Unit,
    onRenameContactClicked: () -> Unit,
    onShareContactClicked: () -> Unit,
    onGroupActionClicked: (MessageType) -> Unit
) {
    var isExpanded by remember(key1 = expanded) { mutableStateOf(expanded) }

    BasicDropdownMenu(
        expanded = isExpanded,
        onToggle = { value -> isExpanded = value }
    ) {
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(id = R.string.rename),
            text = stringResource(id = R.string.rename),
            visible = (isGroup && isMember && isAdmin) || !isGroup,
            onClick = {
                onRenameContactClicked()
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Share,
            contentDescription = stringResource(id = R.string.share),
            text = stringResource(id = R.string.share),
            visible = !isGroup,
            onClick = {
                onShareContactClicked()
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.add_channel_member),
            text = stringResource(id = R.string.add_channel_member),
            visible = isGroup && isMember && isAdmin,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_ADD)
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Clear,
            contentDescription = stringResource(id = R.string.remove_channel_member),
            text = stringResource(id = R.string.remove_channel_member),
            visible = isGroup && isMember && isAdmin,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_REMOVE)
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = stringResource(id = R.string.leave_channel),
            text = stringResource(id = R.string.leave_channel),
            visible = isGroup && isMember && !isAdmin,
            onClick = {
                onGroupActionClicked(MessageType.GROUP_MEMBER_LEAVE)
                isExpanded = false
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
                    isExpanded = false
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
        connectionStatus = ClientStatus.NotConnected,
        contact = RequestState.Success(
            ContactWithGroupDto(
                ContactDtoFactory.createContact(
                    address = "123456-5678-5678-123456",
                    name = "John",
                    publicKey = "public-key",
                    guardHostname = "guard-hostname",
                    guardAddress = "guard-address",
                ), null
            )
        ),
        isMember = true,
        isAdmin = false,
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateBack = {},
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
        connectionStatus = ClientStatus.NotConnected,
        contact = RequestState.Success(
            ContactWithGroupDto(
                ContactDtoFactory.createContact(
                    address = "123456-5678-5678-123456",
                    name = "John",
                    publicKey = "public-key",
                    guardHostname = "guard-hostname",
                    guardAddress = "guard-address",
                ), null
            )
        ),
        isMember = true,
        isAdmin = false,
        onRetryConnection = {},
        onDeleteAllClicked = {},
        onRenameContactClicked = {},
        navigateBack = {},
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
