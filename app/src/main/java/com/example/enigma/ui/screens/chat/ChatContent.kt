package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.util.DatabaseRequestState

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    message: String,
    messages: DatabaseRequestState<List<MessageEntity>>,
    onInputMessageChanged: (String) -> Unit,
    onSendPressed: () -> Unit
)
{
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            if(messages is DatabaseRequestState.Success)
            {
                DisplayMessages(
                    modifier = Modifier.weight(9f),
                    messages = messages.data
                )
            } else
            {
                // TODO: Inform the user that there is no message to be displayed
            }

            ChatInput(
                modifier = Modifier.weight(1f),
                text = message,
                onTextChanged = onInputMessageChanged,
                onSendPressed = onSendPressed
            )
        }
    }
}

@Composable
fun DisplayMessages(
    modifier: Modifier,
    messages: List<MessageEntity>
){
    LazyColumn (
        modifier = modifier
    ){
        items(
            items = messages,
            key = {
                    message -> message.id
            }
        ) {
            message ->
            MessageItem(
                message = message
            )
        }
    }
}
