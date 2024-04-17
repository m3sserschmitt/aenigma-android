package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.ui.screens.common.AutoScrollItemsList
import com.example.enigma.util.DatabaseRequestState
import java.util.Date

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    messages: DatabaseRequestState<List<MessageEntity>>,
    selectedMessages: List<MessageEntity>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onMessageSelected: (MessageEntity) -> Unit,
    onMessageDeselected: (MessageEntity) -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
    ) {
        Column {
            DisplayMessages(
                modifier = Modifier.weight(1f),
                isSelectionMode = isSelectionMode,
                messages = messages,
                selectedMessages = selectedMessages,
                onItemSelected = onMessageSelected,
                onItemDeselected = onMessageDeselected
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
    isSelectionMode: Boolean,
    messages: DatabaseRequestState<List<MessageEntity>>,
    selectedMessages: List<MessageEntity>,
    onItemSelected: (MessageEntity) -> Unit,
    onItemDeselected: (MessageEntity) -> Unit
){
    if(messages is DatabaseRequestState.Success) {
        if(messages.data.isNotEmpty())
        {
            AutoScrollItemsList(
                modifier = modifier,
                items = messages.data,
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
                itemEqualityChecker = { m1, m2 -> m1.id == m2.id},
                itemKeyProvider = { m -> m.id }
            )
        } else {
            NoMessageAvailable(modifier)
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
        isSelectionMode = false,
        messageInputText = "Can't wait to see you on Monday",
        onSendClicked = {},
        onInputTextChanged = {},
        selectedMessages = listOf(),
        onMessageDeselected = { },
        onMessageSelected = { }
    )
}
