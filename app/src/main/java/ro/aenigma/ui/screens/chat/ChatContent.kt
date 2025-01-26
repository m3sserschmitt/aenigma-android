package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.ui.screens.common.AutoScrollItemsList
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.util.DatabaseRequestState
import ro.aenigma.util.PrettyDateFormatter
import java.time.ZoneId

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    replyToMessage: MessageEntity?,
    notSentMessages: List<MessageEntity>,
    nextConversationPageAvailable: Boolean,
    selectedMessages: List<MessageEntity>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onReplyAborted: () -> Unit,
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
                contact = contact,
                notSentMessages = notSentMessages,
                conversationListState = conversationListState,
                nextConversationPageAvailable = nextConversationPageAvailable,
                selectedMessages = selectedMessages,
                onItemSelected = onMessageSelected,
                onItemDeselected = onMessageDeselected,
                loadNextPage = loadNextPage
            )

            ChatInput(
                modifier = Modifier.height(80.dp),
                messageInputText = messageInputText,
                contact = contact,
                replyToMessage = replyToMessage,
                onInputTextChanged = onInputTextChanged,
                onSendClicked = {
                    onSendClicked()
                    messageSent = true
                },
                onReplyAborted = onReplyAborted
            )
        }
    }
}

@Composable
fun MessageDate(next: MessageEntity?, message: MessageEntity) {
    val localDate1 = next?.date?.withZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()
    val localDate2 = message.date.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

    if (localDate1 == null || localDate1 != localDate2) {
        Text(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = PrettyDateFormatter.formatPastDate(message.date),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun DisplayMessages(
    modifier: Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: DatabaseRequestState<List<MessageEntity>>,
    contact: DatabaseRequestState<ContactEntity>,
    notSentMessages: List<MessageEntity>,
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
                            isSent = notSentMessages.contains(messageEntity),
                            message = messageEntity,
                            contact = contact,
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
        is DatabaseRequestState.Error -> GenericErrorScreen(modifier)
        is DatabaseRequestState.Idle -> {  }
    }
}

@Preview
@Composable
fun ChatContentPreview() {
    val message1 = MessageEntity(
        chatId = "123",
        text = "Hey",
        incoming = true,
        sent = false,
        deleted = false,
        uuid = null
    )
    val message2 = MessageEntity(
        chatId = "123",
        text = "Hey, how are you?",
        incoming = false,
        sent = true,
        deleted = false,
        uuid = null
    )
    message1.id = 1
    message2.id = 2

    ChatContent(
        messages = DatabaseRequestState.Success(
            listOf(message1, message2)
        ),
        replyToMessage = null,
        contact = DatabaseRequestState.Idle,
        notSentMessages = listOf(),
        nextConversationPageAvailable = true,
        isSelectionMode = false,
        messageInputText = "Can't wait to see you on Monday",
        onSendClicked = {},
        onReplyAborted = {},
        onInputTextChanged = {},
        selectedMessages = listOf(),
        onMessageDeselected = { },
        onMessageSelected = { },
        isSearchMode = false,
        loadNextPage = {},
    )
}
