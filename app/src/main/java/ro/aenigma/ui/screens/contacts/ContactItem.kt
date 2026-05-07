package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithLastMessageDto
import ro.aenigma.models.MessageDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.MessageDtoExtensions.getMessageTextByAction
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.models.factories.MessageDtoFactory
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.util.StringExtensions.getHost
import java.time.ZonedDateTime

@Composable
fun ContactItem(
    contact: ContactDto,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onItemSelected: (ContactDto) -> Unit = { },
    onItemDeselected: (ContactDto) -> Unit = { },
    onClick: (ContactDto) -> Unit = { },
    additionalInfo: @Composable ColumnScope.() -> Unit = { }
) {
    Surface(
        modifier = Modifier
            .selectable(
                item = contact,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onItemSelected = onItemSelected,
                onItemDeselected = onItemDeselected,
                onClick = onClick
            ).fillMaxWidth()
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                if (isSelected) {
                    Icon(
                        modifier = Modifier.alpha(.5f),
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                } else {
                    Icon(
                        modifier = Modifier.alpha(.5f),
                        painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            if(contact.type == ContactType.CONTACT)
            {
                Icon(
                    modifier = Modifier.weight(1f).fillMaxSize().alpha(.75f),
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = stringResource(
                        id = R.string.contact
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            } else {
                Icon(
                    modifier = Modifier.weight(1f).fillMaxSize().alpha(.75f),
                    painter = painterResource(id = R.drawable.ic_group),
                    contentDescription = stringResource(
                        id = R.string.contact
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            val contactNameTextWeight = if (contact.hasNewMessage) 8f else 9f

            Column(
                modifier = Modifier
                    .weight(contactNameTextWeight)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = contact.name.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                ContactGuardHost(
                    contact = contact
                )
                additionalInfo()
            }

            if (contact.hasNewMessage) {
                Icon(
                    modifier = Modifier.weight(1f).alpha(.5f),
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(id = R.string.new_message),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun ContactWithChatPreviewItem(
    contact: ContactWithLastMessageDto,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onItemSelected: (ContactWithLastMessageDto) -> Unit = { },
    onItemDeselected: (ContactWithLastMessageDto) -> Unit = { },
    onClick: (ContactWithLastMessageDto) -> Unit = { },
) {
    ContactItem(
        contact = contact.contact,
        isSelectionMode = isSelectionMode,
        isSelected = isSelected,
        onItemSelected = { onItemSelected(contact) },
        onItemDeselected = { onItemDeselected(contact) },
        onClick = { onClick(contact) }
    ) {
        if(contact.lastMessage != null) {
            ConversationPreview(
                message = contact.lastMessage
            )
        }
    }
}

@Composable
fun ContactGuardHost(
    contact: ContactDto
) {
    val guardHost = contact.guardHostname.getHost()
    if(!guardHost.isNullOrBlank()) {
        Text(
            modifier = Modifier.alpha(.75f),
            text = "@$guardHost",
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ConversationPreview(
    message: MessageDto
) {
    if (!message.deleted) {
        val context = LocalContext.current
        val youString = stringResource(id = R.string.you)
        val text = remember(key1 = message) {
            if (!message.incoming) {
                "$youString ${message.getMessageTextByAction(context)}"
            } else {
                message.getMessageTextByAction(context)
            }
        }
        if (!text.isNullOrBlank()) {
            Text(
                modifier = Modifier.alpha(.75f),
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
@Preview
fun ContactWithChatPreviewItemPreview() {
    ContactWithChatPreviewItem(
        contact = ContactWithLastMessageDto(
            ContactDtoFactory.createContact(
                address = "12345-5678-5678-12345",
                name = "John",
                publicKey = "public-key",
                guardHostname = "guard-hostname",
                guardAddress = "guard-address",
            ), MessageDtoFactory.createOutgoing(
                chatId = "12345-5678-5678-12345",
                text = "Hello",
                type = MessageType.TEXT,
                actionFor = null,
            )
        ),
        isSelectionMode = false,
        isSelected = false,
        onClick = {},
        onItemSelected = {},
        onItemDeselected = {}
    )
}

@Composable
@Preview
fun ContactWithChatPreviewItemSelectedPreview()
{
    ContactWithChatPreviewItem(
        contact = ContactWithLastMessageDto(
            ContactDtoFactory.createContact(
                address = "12345-5678-5678-12345",
                name = "John",
                publicKey = "public-key",
                guardHostname = "guard-hostname",
                guardAddress = "guard-address",
            ), MessageDtoFactory.createIncoming(
                chatId = "12345-5678-5678-12345",
                senderAddress = "12345-5678-5678-12345",
                text = "Hey, how are you",
                serverUUID = null,
                refId = null,
                actionFor = null,
                dateReceivedOnServer = ZonedDateTime.now(),
                type = MessageType.TEXT
            )
        ),
        isSelectionMode = true,
        isSelected = true,
        onClick = {},
        onItemSelected = {},
        onItemDeselected = {}
    )
}
