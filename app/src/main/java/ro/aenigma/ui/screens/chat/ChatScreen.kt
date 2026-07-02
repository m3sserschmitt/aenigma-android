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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.services.ClientStatus
import ro.aenigma.ui.screens.common.SnackBar
import ro.aenigma.ui.screens.common.ExitSelectionMode
import ro.aenigma.ui.screens.common.RenameContactDialog
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.ChatViewModel
import java.time.ZonedDateTime
import kotlin.collections.listOf

@Composable
fun ChatScreen(
    chatId: String?,
    chatViewModel: ChatViewModel,
    navigateBack: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit,
    navigateToArticle: (uri: String, title: String?, messageId: Long?) -> Unit,
    redirectUri: (String) -> Unit
) {
    LaunchedEffect(key1 = true) {
        chatViewModel.collectSelectedContact(chatId)
        chatViewModel.loadConversation(chatId)
    }

    val selectedContact by chatViewModel.selectedContact.collectAsState()
    val messages by chatViewModel.conversation.collectAsState()
    val replyToMessage by chatViewModel.replyToMessage.collectAsState()
    val messageInputText by chatViewModel.messageInputText.collectAsState()
    val attachments by chatViewModel.attachments.collectAsState()
    val connectionStatus by chatViewModel.clientStatus.collectAsState()
    val isClientWorkerRunning by chatViewModel.isClientWorkerRunning.collectAsState()
    val nextConversationPageAvailable by chatViewModel.nextPageAvailable.collectAsState()
    val contacts by chatViewModel.contacts.collectAsState()
    val isMember by chatViewModel.isMember.collectAsState()
    val isAdmin by chatViewModel.isAdmin.collectAsState()

    MarkConversationAsRead(
        messages = messages,
        chatViewModel = chatViewModel
    )

    ChatScreen(
        contact = selectedContact,
        okHttpClientProvider = chatViewModel.provideOkHttpClientProvider(),
        isMember = isMember,
        isAdmin = isAdmin,
        contacts = contacts,
        connectionStatus = connectionStatus,
        isClientWorkerRunning = isClientWorkerRunning,
        replyToMessage = replyToMessage,
        messages = messages,
        nextConversationPageAvailable = nextConversationPageAvailable,
        messageInputText = messageInputText,
        attachments = attachments,
        onContactSearchQueryChanged = { searchQuery -> chatViewModel.searchContacts(searchQuery) },
        onRetryConnection = { chatViewModel.syncAndReconnect() },
        onInputTextChanged = { newInputTextValue ->
            chatViewModel.setMessageInputText(newInputTextValue)
        },
        onAttachmentsSelected = { attachments -> chatViewModel.setAttachments(attachments) },
        onNewContactNameChanged = { name -> name.isNotBlank() },
        onRenameContactConfirmed = { name -> chatViewModel.renameContact(name) },
        onRenameContactDismissed = { },
        onSendClicked = { chatViewModel.sendMessage() },
        onDeleteAll = { chatViewModel.clearConversation() },
        onDelete = { selectedMessages -> chatViewModel.removeMessages(selectedMessages) },
        onReplyToMessage = { selectedMessage -> chatViewModel.setReplyTo(selectedMessage) },
        onSearch = { searchQuery -> chatViewModel.searchConversation(searchQuery) },
        onAddGroupMembers = { members, action -> chatViewModel.editGroupMembers(members, action) },
        onLeaveGroup = { chatViewModel.leaveGroup() },
        onMessageClicked = { message -> chatViewModel.onMessageClicked(message) },
        onArticleClicked = { article ->
            if (!article.url.isNullOrBlank()) {
                navigateToArticle(article.url, article.title, article.messageId)
            }
        },
        onRedirectUriClicked = { uri ->
            chatViewModel.markConversationAsRead()
            redirectUri(uri)
        },
        loadNextPage = { chatViewModel.loadNextPage() },
        navigateBack = {
            chatViewModel.markConversationAsRead()
            navigateBack()
        },
        navigateToAddContactsScreen = { address ->
            chatViewModel.markConversationAsRead()
            navigateToAddContactsScreen(address)
        }
    )
}

@Composable
fun ChatScreen(
    contact: RequestState<ContactWithGroupDto>,
    okHttpClientProvider: IOkHttpClientProvider,
    isMember: Boolean,
    isAdmin: Boolean,
    contacts: RequestState<List<ContactDto>>,
    connectionStatus: ClientStatus,
    isClientWorkerRunning: Boolean = false,
    messages: RequestState<List<MessageWithDetailsDto>>,
    replyToMessage: RequestState<MessageWithDetailsDto>,
    nextConversationPageAvailable: Boolean,
    messageInputText: String,
    attachments: List<String>,
    onContactSearchQueryChanged: (String) -> Unit,
    onRetryConnection: () -> Unit,
    onInputTextChanged: (String) -> Unit,
    onAttachmentsSelected: (List<String>) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onRenameContactConfirmed: (String) -> Unit,
    onRenameContactDismissed: () -> Unit,
    onSendClicked: () -> Unit,
    onDeleteAll: () -> Unit,
    onDelete: (List<MessageWithDetailsDto>) -> Unit,
    onReplyToMessage: (MessageWithDetailsDto?) -> Unit,
    onSearch: (String) -> Unit,
    onAddGroupMembers: (List<ContactDto>, MessageType) -> Unit = { _, _ -> },
    onLeaveGroup: () -> Unit,
    onMessageClicked: (MessageWithDetailsDto) -> Unit,
    onArticleClicked: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { },
    loadNextPage: () -> Unit,
    navigateBack: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var clearConversationConfirmationVisible by remember { mutableStateOf(false) }
    var deleteMessagesConfirmationVisible by remember { mutableStateOf(false) }
    var addGroupMemberDialogVisible by remember { mutableStateOf(false) }
    var leaveGroupDialogVisible by remember { mutableStateOf(false) }
    var groupAction by remember { mutableStateOf(MessageType.GROUP_MEMBER_ADD) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateMapOf<Long, MessageWithDetailsDto>() }
    val snackBarHostState = remember { SnackbarHostState() }

    AddGroupMemberDialog(
        action = groupAction,
        visible = addGroupMemberDialogVisible,
        contactWithGroup = contact,
        contacts = contacts,
        onConfirmClicked = { members ->
            addGroupMemberDialogVisible = false
            onAddGroupMembers(members, groupAction)
        },
        onDismissClicked = {
            addGroupMemberDialogVisible = false
        },
        onSearchQueryChanged = onContactSearchQueryChanged
    )

    LeaveGroupDialog(
        visible = leaveGroupDialogVisible,
        onConfirmClicked = {
            leaveGroupDialogVisible = false
            onLeaveGroup()
        },
        onDismissClicked = {
            leaveGroupDialogVisible = false
        }
    )

    RenameContactDialog(
        visible = renameContactDialogVisible,
        onNewContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            renameContactDialogVisible = false
            onRenameContactConfirmed(name)
        },
        onDismiss = {
            onRenameContactDismissed()
            renameContactDialogVisible = false
        }
    )

    EraseConversationDialog(
        visible = clearConversationConfirmationVisible,
        onConfirmClicked = {
            clearConversationConfirmationVisible = false
            selectedItems.clear()
            isSelectionMode = false
            onDeleteAll()
        },
        onDismissClicked = {
            clearConversationConfirmationVisible = false
        }
    )

    DeleteSelectedMessagesDialog(
        visible = deleteMessagesConfirmationVisible,
        onConfirmClicked = {
            onDelete(selectedItems.values.toList())
            deleteMessagesConfirmationVisible = false
            selectedItems.clear()
            isSelectionMode = false
        },
        onDismissClicked = {
            deleteMessagesConfirmationVisible = false
        }
    )

    ExitSelectionMode(
        isSelectionMode = isSelectionMode,
        selectedItemsCount = selectedItems.size,
        onSelectionModeExited = {
            isSelectionMode = false
        }
    )

    SnackBar(
        message = stringResource(id = R.string.connection_failed),
        actionLabel = stringResource(id = R.string.retry),
        visible = connectionStatus is ClientStatus.Error.Aborted,
        snackBarHostState = snackBarHostState,
        onActionPerformed = onRetryConnection
    )

    BackHandler(
        enabled = true
    ) {
        if(!isSelectionMode && !isSearchMode) {
            navigateBack()
        }

        if (isSearchMode) {
            isSearchMode = false
        }

        if (isSelectionMode) {
            selectedItems.clear()
            isSelectionMode = false
        }
    }

    LaunchedEffect(key1 = isSearchMode) {
        if(!isSearchMode)
        {
            onSearch("")
        }
    }

    Scaffold (
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        topBar = {
            ChatAppBar(
                messages = messages,
                contact = contact,
                isMember = isMember,
                isAdmin = isAdmin,
                connectionStatus = connectionStatus,
                isClientWorkerRunning = isClientWorkerRunning,
                isSelectionMode = isSelectionMode,
                onRenameContactClicked = {
                    renameContactDialogVisible = true
                },
                onDeleteAllClicked = {
                    clearConversationConfirmationVisible = true
                },
                selectedItemsCount = selectedItems.size,
                onSelectionModeExited = {
                    selectedItems.clear()
                },
                onDeleteClicked = {
                    deleteMessagesConfirmationVisible = true
                },
                onReplyToMessageClicked = {
                    val selectedItem = selectedItems.values.firstOrNull()
                    if(selectedItem != null)
                    {
                        onReplyToMessage(selectedItem)
                    }
                    selectedItems.clear()
                },
                navigateBack = navigateBack,
                isSearchMode = isSearchMode,
                onSearchModeClosed = {
                    isSearchMode = false
                    onSearch("")
                },
                onSearchClicked = {
                    searchQuery -> onSearch(searchQuery)
                },
                onSearchModeTriggered = {
                    isSearchMode = true
                },
                onGroupActionClicked = { action ->
                    when (action) {
                        MessageType.GROUP_MEMBER_ADD, MessageType.GROUP_MEMBER_REMOVE -> {
                            groupAction = action
                            addGroupMemberDialogVisible = true
                            onContactSearchQueryChanged("")
                        }

                        MessageType.GROUP_MEMBER_LEAVE -> {
                            leaveGroupDialogVisible = true
                        }

                        else -> {}
                    }
                },
                onRetryConnection = onRetryConnection,
                navigateToAddContactsScreen = navigateToAddContactsScreen
            )
        },
        content = { paddingValues ->
            ChatContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 8.dp,
                    end = 8.dp
                ),
                okHttpClientProvider = okHttpClientProvider,
                isMember = isMember,
                isSelectionMode = isSelectionMode,
                isSearchMode = isSearchMode,
                replyToMessage = replyToMessage,
                messages = messages,
                nextConversationPageAvailable = nextConversationPageAvailable,
                selectedMessages = selectedItems,
                messageInputText = messageInputText,
                attachments = attachments,
                onInputTextChanged = onInputTextChanged,
                onAttachmentsSelected = onAttachmentsSelected,
                onSendClicked = {
                    onSendClicked()
                    selectedItems.clear()
                },
                onReplyAborted = {
                    onReplyToMessage(null)
                },
                onMessageSelected = { selectedMessage ->
                    if(!isSelectionMode)
                    {
                        isSelectionMode = true
                    }

                    selectedItems[selectedMessage.message.id] = selectedMessage
                },
                onMessageDeselected = {
                    deselectedMessage -> selectedItems.remove(deselectedMessage.message.id)
                },
                onMessageClicked = onMessageClicked,
                onArticleClicked = onArticleClicked,
                onRedirectUriClicked = onRedirectUriClicked,
                loadNextPage = loadNextPage
            )
        }
    )
}

@Composable
fun MarkConversationAsRead(
    messages: RequestState<List<MessageWithDetailsDto>>,
    chatViewModel: ChatViewModel
) {
    LaunchedEffect(key1 = messages)
    {
        if (messages is RequestState.Success) {
            chatViewModel.markConversationAsRead()
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview() {
    val message3 = MessageWithDetailsDto(
        MessageDto(
            chatId = "123",
            senderAddress = "123",
            text = "Hey",
            serverUUID = null,
            type = MessageType.TEXT,
            refId = null,
            actionFor = null,
            dateReceivedOnServer = ZonedDateTime.now(),
            id = 1,
            incoming = true,
            sent = true,
            deleted = false,
            date = ZonedDateTime.now(),
            files = listOf()
        ), null, null
    )
    val message2 = MessageWithDetailsDto(
        MessageDto(
            chatId = "123",
            text = "See you tomorrow at 3 p.m",
            type = MessageType.TEXT,
            actionFor = null,
            id = 2,
            senderAddress = "123",
            serverUUID = null,
            refId = null,
            incoming = true,
            sent = true,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = ZonedDateTime.now(),
            files = listOf()
        ), null, null
    )

    val message1 = MessageWithDetailsDto(
        MessageDto(
            chatId = "123",
            text = "Awesome!",
            type = MessageType.TEXT,
            actionFor = null,
            id = 3,
            senderAddress = null,
            serverUUID = null,
            refId = null,
            incoming = false,
            sent = true,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = ZonedDateTime.now(),
            files = listOf()
        ), null, null
    )

    ChatScreen(
        contact = RequestState.Success(
            ContactWithGroupDto(
                ContactDtoFactory.createContact(
                    address = "123",
                    name = "John",
                    publicKey = null,
                    guardHostname = null,
                    guardAddress = null,
                ), null
            )
        ),
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isMember = true,
        isAdmin = false,
        contacts = RequestState.Success(listOf()),
        connectionStatus = ClientStatus.Authenticated,
        replyToMessage = RequestState.Idle,
        messages = RequestState.Success(
            listOf(message1, message2, message3)
        ),
        nextConversationPageAvailable = true,
        onRetryConnection = {},
        messageInputText = "",
        attachments = listOf(),
        onContactSearchQueryChanged = { },
        onAttachmentsSelected = { },
        onSendClicked = {},
        onRenameContactConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = { true },
        onDeleteAll = {},
        onDelete = {},
        onReplyToMessage = {},
        onSearch = {},
        onLeaveGroup = { },
        onRenameContactDismissed = {},
        loadNextPage = { },
        onMessageClicked = {},
        navigateBack = {},
        navigateToAddContactsScreen = {}
    )
}

@Preview
@Composable
fun ChatScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        ChatScreenPreview()
    }
}
