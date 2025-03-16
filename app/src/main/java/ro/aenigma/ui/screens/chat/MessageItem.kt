package ro.aenigma.ui.screens.chat

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.MessageEntity
import ro.aenigma.data.database.extensions.MessageEntityExtensions.getMessageTextByAction
import ro.aenigma.models.MessageAction
import ro.aenigma.models.enums.ContactType
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.models.enums.MessageActionType
import ro.aenigma.util.PrettyDateFormatter
import ro.aenigma.util.RequestState
import java.time.ZonedDateTime

@Composable
fun MessageItem(
    message: MessageEntity,
    allContacts: List<ContactEntity>,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (MessageEntity) -> Unit,
    onItemDeselected: (MessageEntity) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val text = message.getMessageTextByAction(context)
    val paddingStart = if (message.incoming) 0.dp else 50.dp
    val paddingEnd = if (message.incoming) 50.dp else 0.dp
    val contentColor = if (message.incoming)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer
    val containerColor = if (message.incoming)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer
    val isGroup = message.chatId != message.action.senderAddress
    val replyToMessageMessage by message.responseFor.collectAsState()
    val sender =
        if (isGroup && message.incoming) allContacts.firstOrNull { item -> item.address == message.action.senderAddress } else null
    val deliveryStatus by message.deliveryStatus.collectAsState()
    val isSent = message.sent || deliveryStatus
    Box(
        modifier = Modifier.fillMaxWidth().padding(paddingStart, 8.dp, paddingEnd, 0.dp),
        contentAlignment = if (message.incoming) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .selectable(
                    item = message,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onItemSelected = onItemSelected,
                    onItemDeselected = onItemDeselected,
                    onClick = onClick
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
                    modifier = Modifier.padding(8.dp).width(IntrinsicSize.Max)
                ) {
                    SenderName(
                        contact = sender,
                        color = contentColor
                    )

                    ResponseTo(
                        message = replyToMessageMessage,
                        allContacts = allContacts,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        containerColor = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = text,
                        textAlign = TextAlign.Start,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.alpha(0.5f),
                            textAlign = TextAlign.End,
                            text = PrettyDateFormatter.getTime(message.date),
                            color = contentColor,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (!message.incoming && isSent) {
                            Icon(
                                modifier = Modifier.size(12.dp).alpha(.5f),
                                imageVector = Icons.Outlined.Done,
                                contentDescription = stringResource(R.string.message_sent),
                                tint = contentColor
                            )
                        } else if (!message.incoming) {
                            Icon(
                                modifier = Modifier.size(16.dp).alpha(.5f),
                                painter = painterResource(R.drawable.ic_timer),
                                contentDescription = stringResource(R.string.message_sent),
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
    contact: ContactEntity?,
    color: Color
) {
    if(contact == null)
    {
        return
    }
    Text(
        text = contact.name,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = color
    )
}

@Composable
fun ResponseTo(
    message: RequestState<MessageEntity>,
    allContacts: List<ContactEntity>,
    contentColor: Color,
    containerColor: Color
) {
    if(message !is RequestState.Success)
    {
        return
    }
    val replyToContact =
        allContacts.firstOrNull { item -> item.address == message.data.action.senderAddress }
    val context = LocalContext.current
    val name = if (message.data.incoming && replyToContact != null) replyToContact.name + ":"
    else
        context.getString(R.string.you)
    val text = if (message.data.deleted) context.getString(R.string.message_deleted) else message.data.text
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
                text = text,
                color = contentColor.copy(alpha = .75f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
@Preview
fun GroupSelectionModeNotSelectedIncomingMessagePreview()
{
    MessageItem(
        isSelectionMode = true,
        isSelected = false,
        message = MessageEntity(
            chatId = "123-123-123-124",
            text = "Hello, how are you?",
            incoming = true,
            sent = true,
            uuid = null,
            action = MessageAction(MessageActionType.TEXT, null, "123-123-123-123")
        ),
        allContacts = listOf(ContactEntity(
            address = "123-123-123-123",
            name = "John",
            publicKey = "pkey",
            guardHostname = "hostname",
            guardAddress = "address",
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun GroupSelectionModeIncomingMessageSelectedPreview()
{
    MessageItem(
        isSelectionMode = true,
        isSelected = true,
        message = MessageEntity(
            "123-123-123-124",
            "Hi there!",
            incoming = true,
            sent = false,
            uuid = null,
            action = MessageAction(MessageActionType.TEXT, null, "123-123-123-123")
        ),
        allContacts = listOf(ContactEntity(
            address = "123-123-123-123",
            name = "John",
            publicKey = "pkey",
            guardHostname = "hostname",
            guardAddress = "address",
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessagePending()
{
    MessageItem(
        isSelectionMode = false,
        isSelected = false,
        message = MessageEntity(
            "123-123-123-123",
            "Hello",
            incoming = false,
            sent = false,
            uuid = null,
            action = MessageAction(MessageActionType.TEXT, null, "123-123-123-123")
        ),
        allContacts = listOf(ContactEntity(
            address = "123-123-123-123",
            name = "John",
            publicKey = "pkey",
            guardHostname = "hostname",
            guardAddress = "address",
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessageSent()
{
    MessageItem(
        isSelectionMode = false,
        isSelected = false,
        message = MessageEntity(
            "123-123-123-123",
            "Hello",
            incoming = false,
            sent = true,
            uuid = null,
            action = MessageAction(MessageActionType.TEXT, null, "123-123-123-123")
        ),
        allContacts = listOf(ContactEntity(
            address = "123-123-123-123",
            name = "John",
            publicKey = "pkey",
            guardHostname = "hostname",
            guardAddress = "address",
            type = ContactType.CONTACT,
            hasNewMessage = false,
            lastSynchronized = ZonedDateTime.now()
        )),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}
