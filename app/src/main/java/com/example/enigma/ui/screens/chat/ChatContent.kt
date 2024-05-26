package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.screens.common.AutoScrollItemsList
import com.example.enigma.ui.screens.common.ErrorScreen
import com.example.enigma.ui.screens.common.LoadingScreen
import com.example.enigma.util.DatabaseRequestState
import java.util.Date

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    connectionStatus: SignalRStatus,
    messages: DatabaseRequestState<List<MessageEntity>>,
    nextConversationPageAvailable: Boolean,
    searchedMessages: DatabaseRequestState<List<MessageEntity>>,
    selectedMessages: List<MessageEntity>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onMessageSelected: (MessageEntity) -> Unit,
    onMessageDeselected: (MessageEntity) -> Unit,
    loadNextPage: () -> Unit
) {
    val conversationListState = rememberLazyListState()
    var messageSent by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = messages)
    {
        if(messageSent)
        {
            conversationListState.scrollToItem(0)
            messageSent = false
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            DisplayMessages(
                modifier = Modifier.weight(1f),
                isSelectionMode = isSelectionMode,
                isSearchMode = isSearchMode,
                messages = messages,
                conversationListState = conversationListState,
                nextConversationPageAvailable = nextConversationPageAvailable,
                searchedMessages = searchedMessages,
                selectedMessages = selectedMessages,
                onItemSelected = onMessageSelected,
                onItemDeselected = onMessageDeselected,
                loadNextPage = loadNextPage
            )

            ChatInput(
                modifier = Modifier.height(80.dp),
                enabled = connectionStatus is SignalRStatus.Authenticated,
                messageInputText = messageInputText,
                onInputTextChanged = onInputTextChanged,
                onSendClicked = {
                    onSendClicked()
                    messageSent = true
                }
            )
        }
    }
}

@Composable
fun DisplayMessages(
    modifier: Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: DatabaseRequestState<List<MessageEntity>>,
    nextConversationPageAvailable: Boolean,
    searchedMessages: DatabaseRequestState<List<MessageEntity>>,
    selectedMessages: List<MessageEntity>,
    conversationListState: LazyListState = rememberLazyListState(),
    onItemSelected: (MessageEntity) -> Unit,
    onItemDeselected: (MessageEntity) -> Unit,
    loadNextPage: () -> Unit
) {
    val messagesToDisplay = if(isSearchMode && searchedMessages !is DatabaseRequestState.Idle)
        searchedMessages
    else
        messages

    when(messagesToDisplay)
    {
        is DatabaseRequestState.Success -> {
            if(messagesToDisplay.data.isNotEmpty())
            {
                AutoScrollItemsList(
                    modifier = modifier,
                    items = messagesToDisplay.data,
                    nextPageAvailable = nextConversationPageAvailable,
                    selectedItems = selectedMessages,
                    listItem = { messageEntity, isSelected ->
                        MessageItem(
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            message = messageEntity,
                            onItemSelected = onItemSelected,
                            onItemDeselected = onItemDeselected,
                            onClick = {}
                        )
                    },
                    listState = conversationListState,
                    itemKeyProvider = { m -> m.id },
                    reversedLayout = true,
                    loadNextPage = loadNextPage
                )
            } else {
                if(isSearchMode)
                {
                    EmptySearchResult(modifier)
                }
                else
                {
                    EmptyChatScreen(modifier)
                }
            }
        }
        is DatabaseRequestState.Loading -> LoadingScreen(modifier)
        is DatabaseRequestState.Error -> ErrorScreen(modifier)
        is DatabaseRequestState.Idle -> {  }
    }
}

@Preview
@Composable
fun ChatContentPreview()
{
    val message1 = MessageEntity(chatId = "123", text = "Hey", incoming = true, Date())
    val message2 = MessageEntity(chatId = "123", text = "Hey, how are you?", incoming = false, Date())
    message1.id = 1
    message2.id = 2

    ChatContent(
        messages = DatabaseRequestState.Success(
            listOf(message1, message2)
        ),
        nextConversationPageAvailable = true,
        connectionStatus = SignalRStatus.Authenticated(SignalRStatus.NotConnected()),
        isSelectionMode = false,
        messageInputText = "Can't wait to see you on Monday",
        onSendClicked = {},
        onInputTextChanged = {},
        selectedMessages = listOf(),
        onMessageDeselected = { },
        onMessageSelected = { },
        isSearchMode = false,
        loadNextPage = {},
        searchedMessages = DatabaseRequestState.Success(listOf())
    )
}
