package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.DatabaseRequestState
import java.util.Date

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    messages: DatabaseRequestState<List<MessageEntity>>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            DisplayMessages(
                modifier = Modifier.weight(1f),
                messages = messages
            )

            ChatInput(
                modifier = Modifier.height(80.dp),
                messageInputText = messageInputText,
                onInputTextChanged = onInputTextChanged,
                onSendClicked = onSendClicked
            )
        }
    }
}

@Composable
fun DisplayMessages(
    modifier: Modifier,
    messages: DatabaseRequestState<List<MessageEntity>>
){
    if(messages is DatabaseRequestState.Success) {
        if(messages.data.isNotEmpty())
        {
            MessagesList(
                modifier = modifier,
                messages = messages.data
            )
        } else {
            NoMessageAvailable(modifier)
        }
    }
}

@Composable
fun MessagesList(
    modifier: Modifier = Modifier,
    messages: List<MessageEntity>
) {

    val columnState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        state = columnState
    ) {
        items(
            items = messages,
            key = { message ->
                message.id
            }
        ) { message ->
            MessageItem(
                message = message
            )
        }
    }

    LaunchedEffect(key1 = messages.size)
    {
        if (messages.isNotEmpty()) {
            columnState.scrollToItem(messages.size - 1)
        }
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
        messageInputText = "Can't wait to see you on Monday",
        onSendClicked = {},
        onInputTextChanged = {},
    )
}
