package ro.aenigma.ui.screens.chat

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import kotlinx.coroutines.launch
import ro.aenigma.R
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.attachmentsNotAvailable
import ro.aenigma.models.extensions.MessageDtoExtensions.getMessageTextByAction
import ro.aenigma.models.extensions.MessageWithDetailsDtoExtensions.getDateTime
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.models.factories.MessageDtoFactory
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.ui.screens.common.IndeterminateCircularIndicator
import ro.aenigma.ui.screens.common.FilesList
import ro.aenigma.ui.screens.common.SelectionModeBullet
import java.time.ZonedDateTime
import ro.aenigma.util.ContextExtensions.showImageViewer
import ro.aenigma.util.ZonedDateTimeExtensions.messageCardStyleFormat

@Composable
fun MessageCard(
    modifier: Modifier = Modifier,
    message: MessageWithDetailsDto,
    okHttpClientProvider: IOkHttpClientProvider = OkHttpClientProviderDefault(),
    isOutgoingFailed: Boolean = false,
    isOutgoingSent: Boolean = false,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    deliveryStatusVisible: Boolean = true,
    senderVisible: Boolean = true,
    dateFormatter: (ZonedDateTime) -> String = { date -> date.messageCardStyleFormat() ?: "" },
    onItemSelected: (MessageWithDetailsDto) -> Unit = { },
    onItemDeselected: (MessageWithDetailsDto) -> Unit = { },
    onClick: (MessageWithDetailsDto) -> Unit = { },
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val context = LocalContext.current
    val text = message.message.getMessageTextByAction(context)
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier.selectable(
            item = message,
            isSelectionMode = isSelectionMode,
            isSelected = isSelected,
            onItemSelected = onItemSelected,
            onItemDeselected = onItemDeselected,
            onClick = { item ->
                onClick(item)
                coroutineScope.launch {
                    context.showImageViewer(
                        message = message.message
                    )
                }
            }
        ),
        colors = CardDefaults.cardColors().copy(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionModeBullet(
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                contentColor = contentColor
            )

            Column(
                modifier = Modifier.padding(8.dp).run {
                    if (message.message.type == MessageType.FILES) {
                        fillMaxWidth()
                    } else {
                        width(IntrinsicSize.Max)
                    }
                },
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SenderName(
                    visible = senderVisible,
                    contact = message.sender,
                    color = contentColor
                )

                ResponseTo(
                    message = message,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onRedirectUriClicked = onRedirectUriClicked
                )

                DisplayFiles(
                    message = message.message,
                    textColor = contentColor,
                    okHttpClientProvider = okHttpClientProvider,
                    onRedirectUriClicked = onRedirectUriClicked
                )

                MessageText(
                    text = text,
                    isFiles = message.message.type == MessageType.FILES,
                    contentColor = contentColor
                )

                DisplayLinks(
                    text = text,
                    textColor = contentColor,
                    okHttpClientProvider = okHttpClientProvider,
                    onRedirectUriClicked = onRedirectUriClicked
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if(deliveryStatusVisible) {
                        if (isOutgoingSent) {
                            Icon(
                                modifier = Modifier.size(12.dp).alpha(.5f),
                                imageVector = Icons.Outlined.Done,
                                contentDescription = stringResource(R.string.message_delivery_status),
                                tint = contentColor
                            )
                        } else if (isOutgoingFailed) {
                            ClickToRetryMessage(
                                iconSize = 16.dp,
                                textColor = contentColor,
                                textStyle = MaterialTheme.typography.bodySmall
                            )
                        } else if (!message.message.incoming) {
                            Icon(
                                modifier = Modifier.size(16.dp).alpha(.5f),
                                painter = painterResource(R.drawable.ic_timer),
                                contentDescription = stringResource(R.string.message_delivery_status),
                                tint = contentColor
                            )
                        }
                    }
                    val dateTime = message.getDateTime()
                    if(dateTime != null) {
                        Text(
                            modifier = Modifier.alpha(0.5f),
                            textAlign = TextAlign.End,
                            text = dateFormatter(dateTime),
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberMatchedLinks(text: String): List<String> {
    return remember(key1 = text) {
        val matcher = Patterns.WEB_URL.matcher(text)
        val links = mutableListOf<String>()
        while (matcher.find()) {
            links.add(matcher.group())
        }
        links
    }
}

@Composable
fun DisplayLinks(
    text: String?,
    textColor: Color = Color.Unspecified,
    okHttpClientProvider: IOkHttpClientProvider,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    if(text.isNullOrBlank()) {
        return
    }

    val links = rememberMatchedLinks(text = text)

    if(links.isEmpty()) {
        return
    }

    FilesList(
        uris = links,
        contentColor = textColor,
        okHttpClientProvider = okHttpClientProvider,
        onRedirectUriClicked = onRedirectUriClicked
    )
}

@Composable
fun ClickToRetryMessage(
    iconSize: Dp = 16.dp,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(iconSize)
                .alpha(0.75f),
            imageVector = Icons.Outlined.Warning,
            contentDescription = stringResource(R.string.message_delivery_status),
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = stringResource(id = R.string.click_to_retry),
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = textColor
        )
    }
}

@Composable
fun DisplayFiles(
    message: MessageDto,
    okHttpClientProvider: IOkHttpClientProvider,
    textColor: Color = Color.Unspecified,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    val filesDownloadState by message.attachmentDownloadStatus.collectAsState()
    if(message.attachmentsNotAvailable() && filesDownloadState != WorkInfo.State.SUCCEEDED) {
        if(filesDownloadState == WorkInfo.State.FAILED) {
            ClickToRetryMessage(
                iconSize = 18.dp,
                textColor = textColor,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        } else {
            IndeterminateCircularIndicator(
                visible = true,
                text = stringResource(id = R.string.waiting_for_files),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    } else if(message.type == MessageType.FILES) {
        val filesLate by message.filesLate.collectAsState()
        val files = if(message.files.isNullOrEmpty()) { filesLate } else { message.files }
        FilesList(
            uris = files,
            contentColor = textColor,
            okHttpClientProvider = okHttpClientProvider,
            onRedirectUriClicked = onRedirectUriClicked
        )
    }
}

@Composable
fun SenderName(
    visible: Boolean,
    contact: ContactDto?,
    color: Color
) {
    if(contact == null || !visible)
    {
        return
    }
    Text(
        text = contact.name.toString(),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = color
    )
}

@Composable
fun ResponseTo(
    message: MessageWithDetailsDto?,
    contentColor: Color = Color.Unspecified,
    containerColor: Color = Color.Unspecified,
    onRedirectUriClicked: (String) -> Unit = { }
) {
    if (message == null || message.message.type != MessageType.REPLY || message.actionFor == null
        || message.actionFor.deleted) {
        return
    }
    val actionForSender by message.actionForSender.collectAsState()
    val sender = if (message.actionFor.incoming) {
        actionForSender
    } else {
        ContactDtoFactory.createContact(name = stringResource(R.string.you))
    }
    MessageCard(
        message = MessageWithDetailsDto(message.actionFor, sender, null),
        deliveryStatusVisible = false,
        containerColor = containerColor,
        contentColor = contentColor,
        onRedirectUriClicked = onRedirectUriClicked
    )
}

@Composable
fun MessageText(
    text: String?,
    isFiles: Boolean = false,
    contentColor: Color
) {
    if (text.isNullOrBlank() || isFiles) {
        return
    }
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        textAlign = TextAlign.Start,
        color = contentColor,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
@Preview
fun GroupSelectionModeNotSelectedIncomingMessagePreview() {
    MessageCard(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = true,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageDtoFactory.createIncoming(
                chatId = "123-123-123-124",
                senderAddress = "123-123-123-125",
                text = "Hello, how are you?",
                serverUUID = null,
                refId = null,
                type = MessageType.TEXT,
                actionFor = null,
                dateReceivedOnServer = ZonedDateTime.now(),
            ), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun GroupSelectionModeIncomingMessageSelectedPreview() {
    MessageCard(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = true,
        isSelected = true,
        message = MessageWithDetailsDto(
            MessageDtoFactory.createIncoming(
                chatId = "123-123-123-124",
                senderAddress = "123-123-123-125",
                text = "Hello, how are you?",
                serverUUID = null,
                refId = null,
                type = MessageType.TEXT,
                actionFor = null,
                dateReceivedOnServer = ZonedDateTime.now(),
            ), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessagePending() {
    MessageCard(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = false,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageDtoFactory.createOutgoing(
                "123-123-123-123",
                "Hello",
                type = MessageType.TEXT,
                actionFor = null,
            ), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessageSent() {
    MessageCard(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = false,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageDtoFactory.createOutgoing(
                "123-123-123-123",
                "Hello",
                type = MessageType.TEXT,
                actionFor = null,
            ), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}
