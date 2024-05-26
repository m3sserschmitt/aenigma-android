package com.example.enigma.ui.screens.chat

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
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.screens.common.ConnectionStatusSnackBar
import com.example.enigma.ui.screens.common.ExitSelectionMode
import com.example.enigma.ui.screens.common.RenameContactDialog
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.viewmodels.ChatViewModel
import java.util.Date

@Composable
fun ChatScreen(
    navigateToContactsScreen: () -> Unit,
    chatViewModel: ChatViewModel,
    chatId: String
) {
    LaunchedEffect(key1 = true)
    {
        chatViewModel.loadContacts()
        chatViewModel.loadContact(chatId)
        chatViewModel.loadConversation(chatId)
        chatViewModel.checkPathExistence(chatId)
    }

    val selectedContact by chatViewModel.selectedContact.collectAsState()
    val messages by chatViewModel.conversation.collectAsState()
    val pathsExists by chatViewModel.pathsExist.collectAsState()
    val messageInputText by chatViewModel.messageInputText
    val newContactName by chatViewModel.newContactName
    val connectionStatus by chatViewModel.signalRClientStatus.observeAsState(
        initial = SignalRStatus.NotConnected()
    )
    val nextConversationPageAvailable by chatViewModel.nextPageAvailable.collectAsState()

    MarkConversationAsRead(
        chatId = chatId,
        messages = messages,
        chatViewModel = chatViewModel
    )

    CalculatePath(
        pathsExists = pathsExists,
        selectedContact = selectedContact,
        chatViewModel = chatViewModel
    )

    ChatScreen(
        contact = selectedContact,
        connectionStatus = connectionStatus,
        messages = messages,
        nextConversationPageAvailable = nextConversationPageAvailable,
        messageInputText = messageInputText,
        newContactName = newContactName,
        onRetryConnection = {
            chatViewModel.resetClientStatus()
        },
        onInputTextChanged = {
            newInputTextValue -> chatViewModel.messageInputText.value = newInputTextValue
        },
        onNewContactNameChanged = {
            newContactNameValue -> chatViewModel.updateNewContactName(newContactNameValue)
        },
        onRenameContactConfirmed = {
            chatViewModel.saveNewContact()
        },
        onRenameContactDismissed = {
            chatViewModel.resetNewContactDetails()
        },
        onSendClicked = {
            chatViewModel.sendMessage()
        },
        onDeleteAll = {
            chatViewModel.clearConversation(chatId)
        },
        onDelete = {
            selectedMessages -> chatViewModel.removeMessages(selectedMessages)
        },
        onSearch = {
            searchQuery -> chatViewModel.searchConversation(chatId, searchQuery)
        },
        loadNextPage = {
            chatViewModel.loadNextPage(chatId)
        },
        navigateToContactsScreen = navigateToContactsScreen
    )
}

@Composable
fun ChatScreen(
    contact: DatabaseRequestState<ContactEntity>,
    connectionStatus: SignalRStatus,
    messages: DatabaseRequestState<List<MessageEntity>>,
    nextConversationPageAvailable: Boolean,
    messageInputText: String,
    newContactName: String,
    onRetryConnection: () -> Unit,
    onInputTextChanged: (String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onRenameContactConfirmed: () -> Unit,
    onRenameContactDismissed: () -> Unit,
    onSendClicked: () -> Unit,
    onDeleteAll: () -> Unit,
    onDelete: (List<MessageEntity>) -> Unit,
    onSearch: (String) -> Unit,
    loadNextPage: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var clearConversationConfirmationVisible by remember { mutableStateOf(false) }
    var deleteMessagesConfirmationVisible by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MessageEntity>() }
    val snackBarHostState = remember { SnackbarHostState() }

    SaveNewContactDialog(
        contact = contact,
        newContactName = newContactName,
        onNewContactNameChanged = onNewContactNameChanged,
        onNewNameConfirmClicked = onRenameContactConfirmed,
    )

    RenameContactDialog(
        visible = renameContactDialogVisible,
        newContactName = newContactName,
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
        message = stringResource(id = R.string.conversation_read_only),
        actionLabel = stringResource(id = R.string.retry),
        connectionStatus = connectionStatus,
        targetStatus = SignalRStatus.Aborted::class.java,
        snackBarHostState = snackBarHostState,
        onActionPerformed = onRetryConnection
    )

    BackHandler(
        enabled = isSearchMode || isSelectionMode
    ) {
        if(isSearchMode)
        {
            isSearchMode = false
            onSearch("")
        }

        if(isSelectionMode)
        {
            selectedItems.clear()
            isSelectionMode = false
        }
    }

    LaunchedEffect(key1 = messages)
    {
        if(messages is DatabaseRequestState.Success)
        {
            selectedItems.removeAll { item -> !messages.data.contains(item) }
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
                onRetryConnection = onRetryConnection
            )
        },
        content = { paddingValues ->
            ChatContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                connectionStatus = connectionStatus,
                isSelectionMode = isSelectionMode,
                isSearchMode = isSearchMode,
                messages = messages,
                nextConversationPageAvailable = nextConversationPageAvailable,
                selectedMessages = selectedItems,
                messageInputText = messageInputText,
                onInputTextChanged = onInputTextChanged,
                onSendClicked = onSendClicked,
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
    messages: DatabaseRequestState<List<MessageEntity>>,
    chatViewModel: ChatViewModel
) {
    LaunchedEffect(key1 = messages)
    {
        if(messages is DatabaseRequestState.Success)
        {
            chatViewModel.markConversationAsRead(chatId)
        }
    }
}

@Composable
fun CalculatePath(
    pathsExists: DatabaseRequestState<Boolean>,
    selectedContact: DatabaseRequestState<ContactEntity>,
    chatViewModel: ChatViewModel)
{
    LaunchedEffect(key1 = pathsExists)
    {
        if(pathsExists is DatabaseRequestState.Success
            && selectedContact is DatabaseRequestState.Success)
        {
            if(!pathsExists.data)
            {
                chatViewModel.calculateCircuit()
            }
        }
    }
}

@Preview
@Composable
fun ChatScreenPreview()
{
    val message1 = MessageEntity(chatId = "123", text = "Hey", incoming = true, Date())
    val message2 = MessageEntity(chatId = "123", text = "Hey, how are you?", incoming = false, Date())
    message1.id = 1
    message2.id = 2

    ChatScreen(
        contact = DatabaseRequestState.Success(
            ContactEntity(
                address = "123",
                name = "John",
                publicKey = "key",
                guardHostname = "host",
                hasNewMessage = false
            )
        ),
        connectionStatus = SignalRStatus.Connected(
            previous = SignalRStatus.NotConnected()
        ),
        messages = DatabaseRequestState.Success(
            listOf(message1, message2)
        ),
        nextConversationPageAvailable = true,
        onRetryConnection = {},
        messageInputText = "Can't wait to see you on Monday",
        newContactName = "",
        onSendClicked = {},
        onRenameContactConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = { true },
        onDeleteAll = {},
        onDelete = {},
        onSearch = {},
        onRenameContactDismissed = {},
        loadNextPage = { },
        navigateToContactsScreen = {}
    )
}
