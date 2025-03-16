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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.network.SignalRStatus
import ro.aenigma.models.MessageAction
import ro.aenigma.models.enums.ContactType
import ro.aenigma.ui.screens.common.ConnectionStatusSnackBar
import ro.aenigma.ui.screens.common.ExitSelectionMode
import ro.aenigma.ui.screens.common.RenameContactDialog
import ro.aenigma.models.enums.MessageActionType
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
    val connectionStatus by chatViewModel.signalRClientStatus.observeAsState(
        initial = SignalRStatus.NotConnected()
    )
    val nextConversationPageAvailable by chatViewModel.nextPageAvailable.collectAsState()
    val allContacts by chatViewModel.allContacts.collectAsState()
    val isMember by chatViewModel.isMember.collectAsState()

    MarkConversationAsRead(
        chatId = chatId,
        messages = messages,
        chatViewModel = chatViewModel
    )

    ChatScreen(
        contact = selectedContact,
        isMember = isMember,
        allContacts = allContacts,
        connectionStatus = connectionStatus,
        replyToMessage = replyToMessage,
        messages = messages,
        nextConversationPageAvailable = nextConversationPageAvailable,
        messageInputText = messageInputText,
        onRetryConnection = { chatViewModel.retryClientConnection() },
        onInputTextChanged = { newInputTextValue ->
            chatViewModel.setMessageInputText(
                newInputTextValue
            )
        },
        onNewContactNameChanged = { newContactNameValue ->
            chatViewModel.setNewContactName(
                newContactNameValue
            )
        },
        onRenameContactConfirmed = { chatViewModel.saveContactChanges() },
        onRenameContactDismissed = { chatViewModel.cleanupContactChanges() },
        onSendClicked = { chatViewModel.sendMessage() },
        onDeleteAll = { chatViewModel.clearConversation(chatId) },
        onDelete = { selectedMessages -> chatViewModel.removeMessages(selectedMessages) },
        onReplyToMessage = { selectedMessage -> chatViewModel.setReplyTo(selectedMessage) },
        onSearch = { searchQuery -> chatViewModel.searchConversation(searchQuery) },
        onAddGroupMembers = { members, action -> chatViewModel.editGroupMembers(members, action) },
        onLeaveGroup = { chatViewModel.leaveGroup() },
        loadNextPage = { chatViewModel.loadNextPage(chatId) },
        navigateToContactsScreen = navigateToContactsScreen,
        navigateToAddContactsScreen = navigateToAddContactsScreen
    )
}

@Composable
fun ChatScreen(
    contact: RequestState<ContactWithGroup>,
    isMember: Boolean,
    allContacts: RequestState<List<ContactEntity>>,
    connectionStatus: SignalRStatus,
    messages: RequestState<List<MessageEntity>>,
    replyToMessage: MessageEntity?,
    nextConversationPageAvailable: Boolean,
    messageInputText: String,
    onRetryConnection: () -> Unit,
    onInputTextChanged: (String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onRenameContactConfirmed: () -> Unit,
    onRenameContactDismissed: () -> Unit,
    onSendClicked: () -> Unit,
    onDeleteAll: () -> Unit,
    onDelete: (List<MessageEntity>) -> Unit,
    onReplyToMessage: (MessageEntity?) -> Unit,
    onSearch: (String) -> Unit,
    onAddGroupMembers: (List<String>, MessageActionType) -> Unit,
    onLeaveGroup: () -> Unit,
    loadNextPage: () -> Unit,
    navigateToContactsScreen: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var clearConversationConfirmationVisible by remember { mutableStateOf(false) }
    var deleteMessagesConfirmationVisible by remember { mutableStateOf(false) }
    var addGroupMemberDialogVisible by remember { mutableStateOf(false) }
    var leaveGroupDialogVisible by remember { mutableStateOf(false) }
    var addGroupMembers by remember { mutableStateOf(MessageActionType.GROUP_MEMBER_ADD) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MessageEntity>() }
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
        onConfirmClicked = {
            renameContactDialogVisible = false
            onRenameContactConfirmed()
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
                        MessageActionType.GROUP_MEMBER_ADD, MessageActionType.GROUP_MEMBER_REMOVE -> {
                            addGroupMembers = action
                            addGroupMemberDialogVisible = true
                        }

                        MessageActionType.GROUP_MEMBER_LEFT -> {
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
                onInputTextChanged = onInputTextChanged,
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
                loadNextPage = loadNextPage
            )
        }
    )
}

@Composable
fun MarkConversationAsRead(
    chatId: String,
    messages: RequestState<List<MessageEntity>>,
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
    val message1 = MessageEntity(
        chatId = "123",
        text = "Hey",
        incoming = true,
        sent = false,
        deleted = false,
        uuid = null,
        action = MessageAction(MessageActionType.TEXT, null, "123")
    )
    val message2 = MessageEntity(
        chatId = "123",
        text = "Hey, how are you?",
        incoming = false,
        sent = true,
        deleted = false,
        uuid = null,
        action = MessageAction(MessageActionType.TEXT, null, "123")
    )
    message1.id = 1
    message2.id = 2

    ChatScreen(
        contact = RequestState.Success(
            ContactWithGroup(
                ContactEntity(
                    address = "123",
                    name = "John",
                    publicKey = "key",
                    guardHostname = "host",
                    guardAddress = "guard-address",
                    hasNewMessage = false,
                    type = ContactType.CONTACT,
                    lastSynchronized = ZonedDateTime.now()
                ), null
            )
        ),
        isMember = true,
        allContacts = RequestState.Idle,
        connectionStatus = SignalRStatus.Connected(),
        replyToMessage = null,
        messages = RequestState.Success(
            listOf(message1, message2)
        ),
        nextConversationPageAvailable = true,
        onRetryConnection = {},
        messageInputText = "Can't wait to see you on Monday",
        onSendClicked = {},
        onRenameContactConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = { true },
        onDeleteAll = {},
        onDelete = {},
        onReplyToMessage = {},
        onSearch = {},
        onAddGroupMembers = { _: List<String>, _: MessageActionType -> },
        onLeaveGroup = { },
        onRenameContactDismissed = {},
        loadNextPage = { },
        navigateToContactsScreen = {},
        navigateToAddContactsScreen = {}
    )
}
