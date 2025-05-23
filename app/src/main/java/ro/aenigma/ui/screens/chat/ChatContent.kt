package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
import ro.aenigma.data.database.MessageWithDetails
import ro.aenigma.ui.screens.common.AutoScrollItemsList
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.models.enums.MessageType
import ro.aenigma.util.RequestState
import ro.aenigma.util.PrettyDateFormatter
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    isMember: Boolean,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: RequestState<List<MessageWithDetails>>,
    allContacts: RequestState<List<ContactEntity>>,
    replyToMessage: MessageWithDetails?,
    nextConversationPageAvailable: Boolean,
    selectedMessages: List<MessageWithDetails>,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    onReplyAborted: () -> Unit,
    onMessageSelected: (MessageWithDetails) -> Unit,
    onMessageDeselected: (MessageWithDetails) -> Unit,
    loadNextPage: () -> Unit
) {
    val conversationListState = rememberLazyListState()
    var messageSent by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = messages)
    {
        if (messageSent) {
            conversationListState.scrollToItem(0)
            messageSent = false
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Bottom
    ) {
        DisplayMessages(
            modifier = Modifier.weight(1f),
            isSelectionMode = isSelectionMode,
            isSearchMode = isSearchMode,
            messages = messages,
            allContacts = allContacts,
            conversationListState = conversationListState,
            nextConversationPageAvailable = nextConversationPageAvailable,
            selectedMessages = selectedMessages,
            onItemSelected = onMessageSelected,
            onItemDeselected = onMessageDeselected,
            loadNextPage = loadNextPage
        )

        ChatInput(
            modifier = Modifier.height(80.dp),
            enabled = isMember,
            messageInputText = messageInputText,
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

@Composable
fun MessageDate(next: MessageWithDetails?, message: MessageWithDetails) {
    val localDate1 = next?.message?.date?.withZoneSameInstant(ZoneId.systemDefault())?.toLocalDate()
    val localDate2 = message.message.date.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

    if (localDate1 == null || localDate1 != localDate2) {
        Text(
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = PrettyDateFormatter.formatMessageDateTime(message.message.date),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun DisplayMessages(
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: RequestState<List<MessageWithDetails>>,
    allContacts: RequestState<List<ContactEntity>>,
    nextConversationPageAvailable: Boolean,
    selectedMessages: List<MessageWithDetails>,
    conversationListState: LazyListState = rememberLazyListState(),
    onItemSelected: (MessageWithDetails) -> Unit,
    onItemDeselected: (MessageWithDetails) -> Unit,
    loadNextPage: () -> Unit
) {
    when {
        messages is RequestState.Success && allContacts is RequestState.Success -> {
            if (messages.data.isNotEmpty()) {
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
                            allContacts = allContacts.data,
                            onItemSelected = onItemSelected,
                            onItemDeselected = onItemDeselected,
                            onClick = {}
                        )
                        MessageDate(next = next, message = messageEntity)
                    },
                    listState = conversationListState,
                    itemKeyProvider = { m -> m.message.id },
                    reversedLayout = true,
                    loadNextPage = loadNextPage
                )
            } else {
                if (isSearchMode) {
                    EmptySearchResult(modifier)
                } else {
                    EmptyChatScreen(modifier)
                }
            }
        }

        listOf(messages, allContacts).any { obj -> obj is RequestState.Loading } -> LoadingScreen(
            modifier = modifier
        )

        listOf(messages, allContacts).any { obj -> obj is RequestState.Error } -> GenericErrorScreen(
            modifier = modifier
        )

        else -> {}
    }
}

@Preview
@Composable
fun ChatContentPreview() {
    val message1 = MessageWithDetails(
        MessageEntity(
            chatId = "123",
            senderAddress = "123",
            text = "Hey",
            serverUUID = null,
            type = MessageType.TEXT,
            refId = null,
            actionFor = null,
            dateReceivedOnServer = ZonedDateTime.now(),
            id = 1,
            incoming = true,
            sent = true,
            deleted = false,
            date = ZonedDateTime.now()
        ), null, null
    )
    val message2= MessageWithDetails(
        MessageEntity(
            chatId = "123",
            text = "Please don't forget my green T-shirt...",
            type = MessageType.TEXT,
            actionFor = null,
            id = 2,
            senderAddress = "123",
            serverUUID = null,
            refId = null,
            incoming = true,
            sent = true,
            deleted = false,
            date = ZonedDateTime.now(),
            dateReceivedOnServer = ZonedDateTime.now(),
        ), null, null
    )

    ChatContent(
        messages = RequestState.Success(
            listOf(message1, message2)
        ),
        isMember = true,
        replyToMessage = null,
        nextConversationPageAvailable = true,
        isSelectionMode = false,
        messageInputText = "",
        onSendClicked = {},
        onReplyAborted = {},
        onInputTextChanged = {},
        selectedMessages = listOf(),
        onMessageDeselected = { },
        onMessageSelected = { },
        isSearchMode = false,
        loadNextPage = {},
        allContacts = RequestState.Success(listOf()),
    )
}
