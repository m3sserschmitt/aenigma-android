package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.ui.screens.common.EditContactDialog
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.viewmodels.ChatViewModel

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
    val inputTextState by chatViewModel.inputTextState

    CalculatePath(
        pathsExists = pathsExists,
        selectedContact = selectedContact,
        chatViewModel = chatViewModel
    )

    LaunchedEffect(key1 = messages)
    {
        if(messages is DatabaseRequestState.Success)
        {
            chatViewModel.markConversationAsRead(chatId)
        }
    }

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
                message = inputTextState,
                messages = messages,
                onSendPressed = {
                    chatViewModel.sendMessage()
                },
                onInputMessageChanged = {
                    newText -> chatViewModel.inputTextState.value = newText
                }
            )
            RenameContactDialog(
                contact = selectedContact,
                chatViewModel = chatViewModel
            )
        }
    )
}

@Composable
fun RenameContactDialog(
    contact: DatabaseRequestState<ContactEntity>,
    chatViewModel: ChatViewModel
) {
    val contactName = chatViewModel.newContactName
    if (contact is DatabaseRequestState.Success)
    {
        if(contact.data.name.isEmpty())
        {
            EditContactDialog(
                contactName = contactName.value,
                onContactNameChanged = {
                        newValue -> chatViewModel.newContactName.value = newValue
                },
                title = stringResource(id = R.string.new_contact_available),
                body = stringResource(id = R.string.give_name_to_contact),
                dismissible = false,
                onConfirmClicked = {
                    chatViewModel.saveNewContact()
                },
                onDismissClicked = {  },
                onDismissRequest = { }
            )
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