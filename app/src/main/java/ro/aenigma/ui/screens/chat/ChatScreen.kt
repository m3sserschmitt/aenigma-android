package ro.aenigma.ui.screens.chat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.services.SignalRStatus
import ro.aenigma.ui.screens.common.ConnectionStatusSnackBar
import ro.aenigma.ui.screens.common.ExitSelectionMode
import ro.aenigma.ui.screens.common.RenameContactDialog
import ro.aenigma.models.enums.MessageType
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.ChatViewModel
import java.time.ZonedDateTime

@Composable
fun ChatScreen(
    chatId: String,
    chatViewModel: ChatViewModel,
    navigateToContactsScreen: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    LaunchedEffect(key1 = true)
    {
        chatViewModel.loadContacts(chatId)
        chatViewModel.loadConversation(chatId)
    }

    val selectedContact by chatViewModel.selectedContact.collectAsState()
    val messages by chatViewModel.conversation.collectAsState()
    val replyToMessage by chatViewModel.replyToMessage.collectAsState()
    val messageInputText by chatViewModel.messageInputText.collectAsState()
    val attachments by chatViewModel.attachments.collectAsState()
    val connectionStatus by chatViewModel.clientStatus.collectAsState()
    val nextConversationPageAvailable by chatViewModel.nextPageAvailable.collectAsState()
    val allContacts by chatViewModel.allContacts.collectAsState()
    val isMember by chatViewModel.isMember.collectAsState()
    val isAdmin by chatViewModel.isAdmin.collectAsState()

    MarkConversationAsRead(
        chatId = chatId,
        messages = messages,
        chatViewModel = chatViewModel
    )

    ChatScreen(
        contact = selectedContact,
        isMember = isMember,
        isAdmin = isAdmin,
        allContacts = allContacts,
        connectionStatus = connectionStatus,
        replyToMessage = replyToMessage,
        messages = messages,
        nextConversationPageAvailable = nextConversationPageAvailable,
        messageInputText = messageInputText,
        attachments = attachments,
        onRetryConnection = { chatViewModel.retryClientConnection() },
        onInputTextChanged = { newInputTextValue ->
            chatViewModel.setMessageInputText(newInputTextValue)
        },
        onAttachmentsSelected = { attachments -> chatViewModel.setAttachments(attachments) },
        onNewContactNameChanged = { name -> chatViewModel.validateNewContactName(name) },
        onRenameContactConfirmed = { name -> chatViewModel.renameContact(name) },
        onRenameContactDismissed = {  },
        onSendClicked = { chatViewModel.sendMessage() },
        onDeleteAll = { chatViewModel.clearConversation(chatId) },
        onDelete = { selectedMessages -> chatViewModel.removeMessages(selectedMessages) },
        onReplyToMessage = { selectedMessage -> chatViewModel.setReplyTo(selectedMessage) },
        onSearch = { searchQuery -> chatViewModel.searchConversation(searchQuery) },
        onAddGroupMembers = { members, action -> chatViewModel.editGroupMembers(members, action) },
        onLeaveGroup = { chatViewModel.leaveGroup() },
        onRetryFailed = { message -> chatViewModel.onRetryFailedMessage(message) },
        loadNextPage = { chatViewModel.loadNextPage(chatId) },
        navigateToContactsScreen = navigateToContactsScreen,
        navigateToAddContactsScreen = navigateToAddContactsScreen
    )
}

@Composable
fun ChatScreen(
    contact: RequestState<ContactWithGroup>,
    isMember: Boolean,
    isAdmin: Boolean,
    allContacts: RequestState<List<ContactDto>>,
    connectionStatus: SignalRStatus,
    messages: RequestState<List<MessageWithDetailsDto>>,
    replyToMessage: MessageWithDetailsDto?,
    nextConversationPageAvailable: Boolean,
    messageInputText: String,
    attachments: List<String>,
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
    onAddGroupMembers: (List<String>, MessageType) -> Unit,
    onLeaveGroup: () -> Unit,
    onRetryFailed: (MessageWithDetailsDto) -> Unit,
    loadNextPage: () -> Unit,
    navigateToContactsScreen: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var clearConversationConfirmationVisible by remember { mutableStateOf(false) }
    var deleteMessagesConfirmationVisible by remember { mutableStateOf(false) }
    var addGroupMemberDialogVisible by remember { mutableStateOf(false) }
    var leaveGroupDialogVisible by remember { mutableStateOf(false) }
    var addGroupMembers by remember { mutableStateOf(MessageType.GROUP_MEMBER_ADD) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MessageWithDetailsDto>() }
    val snackBarHostState = remember { SnackbarHostState() }

    AddGroupMemberDialog(
        action = addGroupMembers,
        visible = addGroupMemberDialogVisible,
        contactWithGroup = contact,
        allContacts = allContacts,
        onConfirmClicked = { members ->
            addGroupMemberDialogVisible = false
            onAddGroupMembers(members, addGroupMembers)
        },
        onDismissClicked = {
            addGroupMemberDialogVisible = false
        }
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
            onDeleteAll()
        },
        onDismissClicked = {
            clearConversationConfirmationVisible = false
        }
    )

    DeleteSelectedMessagesDialog(
        visible = deleteMessagesConfirmationVisible,
        onConfirmClicked = {
            onDelete(selectedItems)
            deleteMessagesConfirmationVisible = false
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

    ConnectionStatusSnackBar(
        message = stringResource(id = R.string.connection_failed),
        actionLabel = stringResource(id = R.string.retry),
        connectionStatus = connectionStatus,
        targetStatus = SignalRStatus.Error.Aborted::class.java,
        snackBarHostState = snackBarHostState,
        onActionPerformed = onRetryConnection
    )

    BackHandler(
        enabled = isSearchMode || isSelectionMode
    ) {
        if(isSearchMode)
        {
            isSearchMode = false
        }

        if(isSelectionMode)
        {
            selectedItems.clear()
            isSelectionMode = false
        }
    }

    LaunchedEffect(key1 = messages)
    {
        if(messages is RequestState.Success)
        {
            selectedItems.removeAll { item -> !messages.data.contains(item) }
        }
    }

    LaunchedEffect(key1 = isSearchMode) {
        if(!isSearchMode)
        {
            onSearch("")
        }
    }

    Scaffold (
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
                    val selectedItem = selectedItems.firstOrNull()
                    if(selectedItem != null)
                    {
                        onReplyToMessage(selectedItem)
                    }
                    selectedItems.clear()
                },
                navigateToContactsScreen = navigateToContactsScreen,
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
                            addGroupMembers = action
                            addGroupMemberDialogVisible = true
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
                    start = 4.dp,
                    end = 4.dp
                ),
                isMember = isMember,
                isSelectionMode = isSelectionMode,
                isSearchMode = isSearchMode,
                allContacts = allContacts,
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

                    selectedItems.add(selectedMessage)
                },
                onMessageDeselected = {
                    deselectedMessage -> selectedItems.remove(deselectedMessage)
                },
                onRetryFailed = onRetryFailed,
                loadNextPage = loadNextPage
            )
        }
    )
}

@Composable
fun MarkConversationAsRead(
    chatId: String,
    messages: RequestState<List<MessageWithDetailsDto>>,
    chatViewModel: ChatViewModel
) {
    LaunchedEffect(key1 = messages)
    {
        if(messages is RequestState.Success)
        {
            chatViewModel.markConversationAsRead(chatId)
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
            text = "The green deal is secured =)",
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
            ContactWithGroup(
                ContactEntityFactory.createContact(
                    address = "123",
                    name = "John",
                    publicKey = null,
                    guardHostname = null,
                    guardAddress = null,
                ), null
            )
        ),
        isMember = true,
        isAdmin = false,
        allContacts = RequestState.Success(listOf()),
        connectionStatus = SignalRStatus.Authenticated,
        replyToMessage = null,
        messages = RequestState.Success(
            listOf(message1, message2, message3)
        ),
        nextConversationPageAvailable = true,
        onRetryConnection = {},
        messageInputText = "",
        attachments = listOf(),
        onAttachmentsSelected = { },
        onSendClicked = {},
        onRenameContactConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = { true },
        onDeleteAll = {},
        onDelete = {},
        onReplyToMessage = {},
        onSearch = {},
        onAddGroupMembers = { _: List<String>, _: MessageType -> },
        onLeaveGroup = { },
        onRenameContactDismissed = {},
        loadNextPage = { },
        onRetryFailed = {},
        navigateToContactsScreen = {},
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
