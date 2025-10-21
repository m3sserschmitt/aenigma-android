package ro.aenigma.ui.screens.chat

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import ro.aenigma.R
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.attachmentsNotAvailable
import ro.aenigma.models.extensions.MessageDtoExtensions.getMessageTextByAction
import ro.aenigma.models.extensions.MessageDtoExtensions.isNotSent
import ro.aenigma.services.IOkHttpClientProvider
import ro.aenigma.services.OkHttpClientProviderDefault
import ro.aenigma.ui.screens.common.IndeterminateCircularIndicator
import ro.aenigma.ui.screens.common.FilesList
import ro.aenigma.util.Constants.Companion.ATTACHMENTS_METADATA_FILE
import ro.aenigma.util.PrettyDateFormatter
import java.time.ZonedDateTime

@Composable
fun MessageItem(
    message: MessageWithDetailsDto,
    okHttpClientProvider: IOkHttpClientProvider,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (MessageWithDetailsDto) -> Unit,
    onItemDeselected: (MessageWithDetailsDto) -> Unit,
    onClick: (MessageWithDetailsDto) -> Unit,
) {
    val context = LocalContext.current
    val text = if(message.message.text.isNullOrBlank()) {
        null
    } else {
        message.message.getMessageTextByAction(context)
    }
    val paddingStart = if (message.message.incoming) 0.dp else 50.dp
    val paddingEnd = if (message.message.incoming) 50.dp else 0.dp
    val contentColor = if (message.message.incoming)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer
    val containerColor = if (message.message.incoming)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val sender =
        if (message.message.chatId != message.message.senderAddress && message.message.incoming)
            message.sender
        else null
    val deliveryStatus by message.message.deliveryStatus.collectAsState()
    val isOutgoingSent = !message.message.incoming && (message.message.sent || deliveryStatus == WorkInfo.State.SUCCEEDED)
    val isOutgoingFailed = message.message.isNotSent() && deliveryStatus == WorkInfo.State.FAILED

    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(paddingStart, 8.dp, paddingEnd, 0.dp),
        contentAlignment = if (message.message.incoming) Alignment.CenterStart else Alignment.CenterEnd,
    ) {
        Card(
            modifier = Modifier
                .selectable(
                    item = message,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onItemSelected = onItemSelected,
                    onItemDeselected = onItemDeselected,
                    onClick = { onClick(message) }
                ),
            colors = CardDefaults.cardColors().copy(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    if (isSelected) {
                        Icon(
                            modifier = Modifier.alpha(.5f),
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.message_selection),
                            tint = contentColor,
                        )
                    } else {
                        Icon(
                            modifier = Modifier.alpha(.5f),
                            painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                            contentDescription = stringResource(R.string.message_selection),
                            tint = contentColor,
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(8.dp).run {
                        if(message.message.type == MessageType.FILES) {
                            fillMaxWidth()
                        } else {
                            width(IntrinsicSize.Max)
                        }
                    },
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SenderName(
                        contact = sender,
                        color = contentColor
                    )

                    ResponseTo(
                        message = message,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.secondary
                    )

                    DisplayFiles(
                        message = message.message,
                        textColor = contentColor,
                        okHttpClientProvider = okHttpClientProvider
                    )

                    MessageText(
                        text = text,
                        contentColor = contentColor
                    )

                    DisplayLinks(
                        text = text,
                        textColor = contentColor,
                        okHttpClientProvider = okHttpClientProvider
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
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
                        Text(
                            modifier = Modifier.alpha(0.5f),
                            textAlign = TextAlign.End,
                            text = PrettyDateFormatter.formatTime(message.message.date),
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
    okHttpClientProvider: IOkHttpClientProvider
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
        textColor = textColor,
        okHttpClientProvider = okHttpClientProvider
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
    textColor: Color = Color.Unspecified
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
            .filter { item -> !item.endsWith(ATTACHMENTS_METADATA_FILE) }
        FilesList(
            uris = files,
            textColor = textColor,
            okHttpClientProvider = okHttpClientProvider
        )
    }
}

@Composable
fun SenderName(
    contact: ContactDto?,
    color: Color
) {
    if(contact == null)
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
    contentColor: Color,
    containerColor: Color
) {
    if (message == null) {
        return
    }
    val context = LocalContext.current
    val actionForSender by message.actionForSender.collectAsState()
    if(message.message.type != MessageType.REPLY) {
        return
    }
    val name = if (message.actionFor?.incoming == true) {
        (actionForSender?.name ?: return) + ":"
    } else {
        context.getString(R.string.you)
    }
    val text = if (message.actionFor?.deleted == true) {
        context.getString(R.string.message_deleted)
    } else {
        message.actionFor?.text ?: return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(4.dp)
        ) {
            Text(
                text = name,
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = text,
                color = contentColor.copy(alpha = .75f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MessageText(
    text: String?,
    contentColor: Color
) {
    if(text.isNullOrBlank())
    {
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
    MessageItem(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = true,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageEntityFactory.createIncoming(
                chatId = "123-123-123-124",
                senderAddress = "123-123-123-125",
                text = "Hello, how are you?",
                serverUUID = null,
                refId = null,
                type = MessageType.TEXT,
                actionFor = null,
                dateReceivedOnServer = ZonedDateTime.now(),
            ).toDto(), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun GroupSelectionModeIncomingMessageSelectedPreview() {
    MessageItem(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = true,
        isSelected = true,
        message = MessageWithDetailsDto(
            MessageEntityFactory.createIncoming(
                chatId = "123-123-123-124",
                senderAddress = "123-123-123-125",
                text = "Hello, how are you?",
                serverUUID = null,
                refId = null,
                type = MessageType.TEXT,
                actionFor = null,
                dateReceivedOnServer = ZonedDateTime.now(),
            ).toDto(), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessagePending() {
    MessageItem(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = false,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageEntityFactory.createOutgoing(
                "123-123-123-123",
                "Hello",
                type = MessageType.TEXT,
                actionFor = null,
            ).toDto(), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessageSent() {
    MessageItem(
        okHttpClientProvider = OkHttpClientProviderDefault(),
        isSelectionMode = false,
        isSelected = false,
        message = MessageWithDetailsDto(
            MessageEntityFactory.createOutgoing(
                "123-123-123-123",
                "Hello",
                type = MessageType.TEXT,
                actionFor = null,
            ).toDto(), null, null
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}
