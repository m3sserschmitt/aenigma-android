package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import ro.aenigma.models.ArticleDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.ui.screens.common.AutoScrollItemsList
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.isNotSent
import ro.aenigma.models.extensions.MessageWithDetailsDtoExtensions.getDateTime
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.ui.screens.common.ArticleCard
import ro.aenigma.util.ContextExtensions.getArticle
import ro.aenigma.util.RequestState
import ro.aenigma.util.ZonedDateTimeExtensions.chatroomStyleFormat
import java.time.ZonedDateTime

@Composable
fun ChatContent(
    modifier: Modifier = Modifier,
    okHttpClientProvider: IOkHttpClientProvider,
    isMember: Boolean,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: RequestState<List<MessageWithDetailsDto>>,
    replyToMessage: RequestState<MessageWithDetailsDto>,
    nextConversationPageAvailable: Boolean,
    selectedMessages: Map<Long, MessageWithDetailsDto>,
    messageInputText: String,
    attachments: List<String>,
    onInputTextChanged: (String) -> Unit,
    onAttachmentsSelected: (List<String>) -> Unit,
    onSendClicked: () -> Unit,
    onReplyAborted: () -> Unit,
    onMessageSelected: (MessageWithDetailsDto) -> Unit,
    onMessageDeselected: (MessageWithDetailsDto) -> Unit,
    onMessageClicked: (MessageWithDetailsDto) -> Unit,
    onArticleClicked: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { },
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
        DisplayChat(
            modifier = Modifier.weight(1f),
            okHttpClientProvider = okHttpClientProvider,
            isSelectionMode = isSelectionMode,
            isSearchMode = isSearchMode,
            messages = messages,
            conversationListState = conversationListState,
            nextConversationPageAvailable = nextConversationPageAvailable,
            selectedMessages = selectedMessages,
            onItemSelected = onMessageSelected,
            onItemDeselected = onMessageDeselected,
            onMessageClicked = onMessageClicked,
            onArticleClicked = onArticleClicked,
            onRedirectUriClicked = onRedirectUriClicked,
            loadNextPage = loadNextPage
        )

        ChatInput(
            modifier = Modifier.height(80.dp),
            visible = isMember,
            messageInputText = messageInputText,
            replyToMessage = replyToMessage,
            onInputTextChanged = onInputTextChanged,
            attachments = attachments,
            onAttachmentsSelected = onAttachmentsSelected,
            onSendClicked = {
                onSendClicked()
                messageSent = true
            },
            onReplyAborted = onReplyAborted
        )
    }
}

@Composable
fun MessageDate(next: MessageWithDetailsDto?, message: MessageWithDetailsDto) {
    val localDate1 = next?.getDateTime()?.toLocalDate()
    val localDate2 = message.getDateTime()?.toLocalDate()

    if (localDate1 == null || localDate1 != localDate2) {
        val text = message.message.date.chatroomStyleFormat()
        if(!text.isNullOrBlank()) {
            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun rememberArticle(
    messageWithDetails: MessageWithDetailsDto
): State<ArticleDto?> {
    val context = LocalContext.current
    return produceState(
        key1 = messageWithDetails,
        initialValue = null
    ) {
        value = context.getArticle(messageWithDetails)
    }
}

@Composable
fun ChatItem(
    message: MessageWithDetailsDto,
    okHttpClientProvider: IOkHttpClientProvider,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onItemSelected: (MessageWithDetailsDto) -> Unit = { },
    onItemDeselected: (MessageWithDetailsDto) -> Unit = { },
    onMessageClicked: (MessageWithDetailsDto) -> Unit = { },
    onArticleClicked: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val deliveryStatus by message.message.deliveryStatus.collectAsState()
    val article by rememberArticle(messageWithDetails = message)
    val isOutgoingSent = !message.message.incoming
            && (message.message.sent || deliveryStatus == WorkInfo.State.SUCCEEDED)
    val isOutgoingFailed = !message.message.incoming && message.message.isNotSent()
            && deliveryStatus == WorkInfo.State.FAILED
    val paddingStart = if (message.message.incoming) {
        0.dp
    } else {
        50.dp
    }
    val paddingEnd = if (message.message.incoming) {
        50.dp
    } else {
        0.dp
    }
    val contentColor = if (message.message.incoming) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val containerColor = if (message.message.incoming) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(start = paddingStart, top = 4.dp, end = paddingEnd, bottom = 4.dp),
        contentAlignment = if (message.message.incoming) {
            Alignment.CenterStart
        } else {
            Alignment.CenterEnd
        },
    ) {
        if (article?.url.isNullOrBlank() || isOutgoingFailed) {
            val senderVisible =
                message.message.chatId != message.message.senderAddress && message.message.incoming
            MessageCard(
                okHttpClientProvider = okHttpClientProvider,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                message = message,
                senderVisible = senderVisible,
                isOutgoingSent = isOutgoingSent,
                contentColor = contentColor,
                containerColor = containerColor,
                isOutgoingFailed = isOutgoingFailed,
                onItemSelected = onItemSelected,
                onItemDeselected = onItemDeselected,
                onRedirectUriClicked = onRedirectUriClicked,
                onClick = onMessageClicked,
            )
        } else {
            ArticleCard(
                article = article ?: ArticleDto(),
                contentColor = contentColor,
                containerColor = containerColor,
                isSelected = isSelected,
                isSelectionMode = isSelectionMode,
                okHttpClientProvider = okHttpClientProvider,
                onItemSelected = { onItemSelected(message) },
                onItemDeselected = { onItemDeselected(message) },
                onRedirectUriClicked = onRedirectUriClicked,
                onClick = onArticleClicked
            )
        }
    }
}

@Composable
fun DisplayChat(
    modifier: Modifier = Modifier,
    okHttpClientProvider: IOkHttpClientProvider,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    messages: RequestState<List<MessageWithDetailsDto>>,
    nextConversationPageAvailable: Boolean,
    selectedMessages: Map<Long, MessageWithDetailsDto>,
    conversationListState: LazyListState = rememberLazyListState(),
    onItemSelected: (MessageWithDetailsDto) -> Unit,
    onItemDeselected: (MessageWithDetailsDto) -> Unit,
    onMessageClicked: (MessageWithDetailsDto) -> Unit,
    onArticleClicked: (ArticleDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { },
    loadNextPage: () -> Unit
) {
    when(messages) {
        is RequestState.Success -> {
            if (messages.data.isNotEmpty()) {
                AutoScrollItemsList(
                    modifier = modifier,
                    items = messages.data,
                    nextPageAvailable = nextConversationPageAvailable,
                    selectedItems = selectedMessages,
                    listItem = { next, messageEntity, isSelected ->
                        ChatItem(
                            okHttpClientProvider = okHttpClientProvider,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            message = messageEntity,
                            onItemSelected = onItemSelected,
                            onItemDeselected = onItemDeselected,
                            onRedirectUriClicked = onRedirectUriClicked,
                            onArticleClicked = onArticleClicked,
                            onMessageClicked = onMessageClicked,
                        )
                        MessageDate(next = next, message = messageEntity)
                    },
                    listState = conversationListState,
                    itemKeySelector = { m -> m.message.id },
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

        is RequestState.Loading -> LoadingScreen(
            modifier = modifier
        )

        is RequestState.Error -> GenericErrorScreen(
            modifier = modifier
        )

        else -> {}
    }
}

@Preview
@Composable
fun ChatContentPreview() {
    val message1 = MessageWithDetailsDto(
        MessageDto(
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
            date = ZonedDateTime.now(),
            files = listOf()
        ), null, null
    )
    val message2 = MessageWithDetailsDto(
        MessageDto(
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
            files = listOf()
        ), null, null
    )

    ChatContent(
        messages = RequestState.Success(
            listOf(message1, message2)
        ),
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isMember = true,
        replyToMessage = RequestState.Idle,
        nextConversationPageAvailable = true,
        isSelectionMode = false,
        messageInputText = "",
        attachments = listOf(),
        onAttachmentsSelected = { },
        onSendClicked = {},
        onReplyAborted = {},
        onInputTextChanged = {},
        selectedMessages = mapOf(),
        onMessageDeselected = { },
        onMessageSelected = { },
        isSearchMode = false,
        loadNextPage = {},
        onMessageClicked = {}
    )
}
