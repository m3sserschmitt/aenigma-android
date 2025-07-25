package ro.aenigma.ui.screens.chat

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.WorkInfo
import coil.compose.AsyncImage
import coil.request.ImageRequest
import ro.aenigma.R
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.extensions.MessageEntityExtensions.toDto
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.ContactDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.getMessageTextByAction
import ro.aenigma.util.PrettyDateFormatter
import java.time.ZonedDateTime

@Composable
fun MessageItem(
    message: MessageWithDetailsDto,
    allContacts: List<ContactDto>,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (MessageWithDetailsDto) -> Unit,
    onItemDeselected: (MessageWithDetailsDto) -> Unit,
    onClick: () -> Unit,
    onRetryFailed: (MessageWithDetailsDto) -> Unit
) {
    val context = LocalContext.current
    val text = message.message.getMessageTextByAction(context)
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
    val isOutgoingFailed = !message.message.incoming && !message.message.sent && deliveryStatus == WorkInfo.State.FAILED
    val replyToMessage = if (message.message.type == MessageType.REPLY) message.actionFor else null
    Box(
        modifier = Modifier.fillMaxWidth().padding(paddingStart, 8.dp, paddingEnd, 0.dp),
        contentAlignment = if (message.message.incoming) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .selectable(
                    item = message,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onItemSelected = onItemSelected,
                    onItemDeselected = onItemDeselected,
                    onClick = {
                        if(isOutgoingFailed) {
                            onRetryFailed(message)
                        }
                        onClick()
                    }
                ),
            color = containerColor,
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
                    }
                ) {
                    SenderName(
                        contact = sender,
                        color = contentColor
                    )

                    ResponseTo(
                        message = replyToMessage,
                        allContacts = allContacts,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.secondary
                    )

                    UriListDisplay(
                        uris = message.message.files
                    )

                    MessageText(
                        text = text,
                        contentColor = contentColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.alpha(0.5f),
                            textAlign = TextAlign.End,
                            text = PrettyDateFormatter.formatTime(message.message.date),
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (isOutgoingSent) {
                            Icon(
                                modifier = Modifier.size(12.dp).alpha(.5f),
                                imageVector = Icons.Outlined.Done,
                                contentDescription = stringResource(R.string.message_delivery_status),
                                tint = contentColor
                            )
                        } else if (isOutgoingFailed) {
                            Icon(
                                modifier = Modifier.size(16.dp).alpha(.5f),
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = stringResource(R.string.message_delivery_status),
                                tint = Color.Red
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
                }
            }
        }
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
    message: MessageDto?,
    allContacts: List<ContactDto>,
    contentColor: Color,
    containerColor: Color
) {
    if (message == null) {
        return
    }
    val replyToContact =
        allContacts.firstOrNull { item -> item.address == message.senderAddress }
    val context = LocalContext.current
    val name = if (message.incoming && replyToContact != null) replyToContact.name + ":"
    else
        context.getString(R.string.you)
    val text = if (message.deleted) context.getString(R.string.message_deleted) else message.text
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
                modifier = Modifier.padding(start = 4.dp),
                text = text.toString(),
                color = contentColor.copy(alpha = .75f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MessageText(
    text: String,
    contentColor: Color
) {
    if(text.isBlank())
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
fun UriListDisplay(uris: List<String>?) {
    val context = LocalContext.current
    if(uris != null && uris.isNotEmpty())
    {
        Column {
            uris.forEach { uri ->
                UriItem(uri = uri.toUri(), context = context)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun UriItem(uri: Uri, context: Context) {
    val isImage = remember(uri) { isImageUri(context, uri) }

    if (isImage) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = stringResource(id = R.string.files),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = uri.lastPathSegment ?: stringResource(id = R.string.unknown_file),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Button(onClick = { openUriInExternalApp(context, uri) }) {
                    Text(
                        text = stringResource(id = R.string.open)
                    )
                }
            }
        }
    }
}

fun isImageUri(context: Context, uri: Uri): Boolean {
    return context.contentResolver.getType(uri)?.startsWith("image/") == true
}

fun openUriInExternalApp(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, context.getString(R.string.no_app_to_open), Toast.LENGTH_SHORT).show()
    }
}

@Composable
@Preview
fun GroupSelectionModeNotSelectedIncomingMessagePreview() {
    MessageItem(
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
        allContacts = listOf(
            ContactEntityFactory.createContact(
                address = "123-123-123-123",
                name = "John",
                publicKey = "pkey",
                guardHostname = "hostname",
                guardAddress = "address",
            ).toDto()
        ),
        onItemDeselected = {},
        onClick = {},
        onRetryFailed = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun GroupSelectionModeIncomingMessageSelectedPreview() {
    MessageItem(
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
        allContacts = listOf(
            ContactEntityFactory.createContact(
                address = "123-123-123-123",
                name = "John",
                publicKey = "pkey",
                guardHostname = "hostname",
                guardAddress = "address",
            ).toDto()
        ),
        onItemDeselected = {},
        onClick = {},
        onRetryFailed = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessagePending() {
    MessageItem(
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
        allContacts = listOf(
            ContactEntityFactory.createContact(
                address = "123-123-123-123",
                name = "John",
                publicKey = "pkey",
                guardHostname = "hostname",
                guardAddress = "address",
            ).toDto()
        ),
        onItemDeselected = {},
        onClick = {},
        onRetryFailed = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessageSent() {
    MessageItem(
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
        allContacts = listOf(
            ContactEntityFactory.createContact(
                address = "123-123-123-123",
                name = "John",
                publicKey = "pkey",
                guardHostname = "hostname",
                guardAddress = "address",
            ).toDto()
        ),
        onItemDeselected = {},
        onClick = {},
        onRetryFailed = {},
        onItemSelected = {}
    )
}
