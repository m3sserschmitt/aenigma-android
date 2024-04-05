package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        selectedContact = selectedContact,
        messages = messages,
        messageInputText = messageInputText,
        newContactName = newContactName,
        onInputTextChanged = {
            newInputTextValue -> chatViewModel.messageInputText.value = newInputTextValue
        },
        onNewContactNameChanged = {
            newContactNameValue -> chatViewModel.newContactName.value = newContactNameValue
        },
        onNewContactNameConfirmed = {
            chatViewModel.saveNewContact()
        },
        onSendClicked = {
            chatViewModel.sendMessage()
        },
        navigateToContactsScreen = navigateToContactsScreen
    )
}

@Composable
fun ChatScreen(
    selectedContact: DatabaseRequestState<ContactEntity>,
    messages: DatabaseRequestState<List<MessageEntity>>,
    messageInputText: String,
    newContactName: String,
    onInputTextChanged: (String) -> Unit,
    onNewContactNameChanged: (String) -> Unit,
    onNewContactNameConfirmed: () -> Unit,
    onSendClicked: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    Scaffold (
        topBar = {
            ChatAppBar(
                contact = selectedContact,
                navigateToContactsScreen = navigateToContactsScreen)
        },
        content = { paddingValues ->
            ChatContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                contact = selectedContact,
                messages = messages,
                messageInputText = messageInputText,
                newContactName = newContactName,
                onInputTextChanged = onInputTextChanged,
                onNewContactNameChanged = onNewContactNameChanged,
                onNewNameConfirmClicked = onNewContactNameConfirmed,
                onSendClicked = onSendClicked
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
        selectedContact = DatabaseRequestState.Success(
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
        onNewContactNameConfirmed = {},
        onInputTextChanged = {},
        onNewContactNameChanged = {},
        navigateToContactsScreen = {}
    )
}
