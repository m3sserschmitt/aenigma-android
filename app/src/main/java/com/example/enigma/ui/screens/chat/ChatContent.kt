package com.example.enigma.ui.screens.chat

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.screens.common.AutoScrollItemsList
import com.example.enigma.ui.screens.common.ErrorScreen
import com.example.enigma.ui.screens.common.LoadingScreen
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.PrettyDateFormatter
import java.time.ZoneId
import java.util.Date

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    connectionStatus: SignalRStatus,
    messages: DatabaseRequestState<List<MessageEntity>>,
    nextConversationPageAvailable: Boolean,
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
fun MessageDate(next: MessageEntity?, message: MessageEntity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val localDate1 = next?.date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        val localDate2 = message.date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        if (localDate1 == null || localDate1 != localDate2) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = PrettyDateFormatter.formatPastDate(message.date)
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
    selectedMessages: List<MessageEntity>,
    conversationListState: LazyListState = rememberLazyListState(),
    onItemSelected: (MessageEntity) -> Unit,
    onItemDeselected: (MessageEntity) -> Unit,
    loadNextPage: () -> Unit
) {
    when(messages)
    {
        is DatabaseRequestState.Success -> {
            if(messages.data.isNotEmpty())
            {
                AutoScrollItemsList(
                    modifier = modifier,
                    items = messages.data,
                    nextPageAvailable = nextConversationPageAvailable,
                    selectedItems = selectedMessages,
                    listItem = { next, messageEntity, isSelected ->
                        MessageItem(
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            message = messageEntity,
                            onItemSelected = onItemSelected,
                            onItemDeselected = onItemDeselected,
                            onClick = {}
                        )
                        MessageDate(next = next, message = messageEntity)
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
    )
}
