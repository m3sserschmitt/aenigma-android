package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
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
    val messages by chatViewModel.messages.collectAsState()
    val pathsExists by chatViewModel.pathsExist.collectAsState()
    val messageInputText by chatViewModel.messageInputText
    val newContactName by chatViewModel.newContactName

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
        messages = messages,
        messageInputText = messageInputText,
        newContactName = newContactName,
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
        navigateToContactsScreen = navigateToContactsScreen
    )
}

@Composable
fun ChatScreen(
    contact: DatabaseRequestState<ContactEntity>,
    messages: DatabaseRequestState<List<MessageEntity>>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    newContactName: String,
    onNewContactNameChanged: (String) -> Boolean,
    onRenameContactConfirmed: () -> Unit,
    onRenameContactDismissed: () -> Unit,
    onSendClicked: () -> Unit,
    onDeleteAll: () -> Unit,
    onDelete: (List<MessageEntity>) -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var clearConversationConfirmationVisible by remember { mutableStateOf(false) }
    var deleteMessagesConfirmationVisible by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<MessageEntity>() }

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
        onNewNameConfirmClicked = {
            renameContactDialogVisible = false
            onRenameContactConfirmed()
        },
        onDismissClicked = {
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
        selectedItems = selectedItems,
        onSelectionModeExited = {
            isSelectionMode = false
        }
    )

    LaunchedEffect(key1 = messages)
    {
        if(messages is DatabaseRequestState.Success)
        {
            selectedItems.removeAll { item -> !messages.data.contains(item) }
        }
    }

    Scaffold (
        topBar = {
            ChatAppBar(
                messages = messages,
                contact = contact,
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
                navigateToContactsScreen = navigateToContactsScreen
            )
        },
        content = { paddingValues ->
            ChatContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                isSelectionMode = isSelectionMode,
                messages = messages,
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
                }
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

@Composable
fun ExitSelectionMode(
    isSelectionMode: Boolean,
    selectedItems: List<MessageEntity>,
    onSelectionModeExited: () -> Unit
) {
    LaunchedEffect(key1 = isSelectionMode, key2 = selectedItems.size)
    {
        if(isSelectionMode && selectedItems.isEmpty())
        {
            onSelectionModeExited()
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
        messages = DatabaseRequestState.Success(
            listOf(message1, message2)
        ),
        messageInputText = "Can't wait to see you on Monday",
        newContactName = "",
        onSendClicked = {},
        onRenameContactConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = { true },
        onDeleteAll = {},
        onDelete = {},
        onRenameContactDismissed = {},
        navigateToContactsScreen = {}
    )
}
