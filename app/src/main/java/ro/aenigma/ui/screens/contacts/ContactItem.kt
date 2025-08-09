package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.layout.Column
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
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.extensions.MessageEntityExtensions.getMessageTextByAction
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.ui.screens.common.selectable
import java.time.ZonedDateTime

@Composable
fun ContactItem(
    onItemSelected: (ContactWithLastMessage) -> Unit,
    onItemDeselected: (ContactWithLastMessage) -> Unit,
    onClick: () -> Unit,
    contact: ContactWithLastMessage,
    isSelectionMode: Boolean,
    isSelected: Boolean
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .selectable(
                item = contact,
                isSelectionMode = isSelectionMode,
                isSelected = isSelected,
                onItemSelected = onItemSelected,
                onItemDeselected = onItemDeselected,
                onClick = onClick
            ).fillMaxWidth().height(64.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
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

            if(contact.contact.type == ContactType.CONTACT)
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

            val contactNameTextWeight = if (contact.contact.hasNewMessage) 8f else 9f

            Column(
                modifier = Modifier
                    .weight(contactNameTextWeight)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = contact.contact.name.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if (contact.lastMessage != null && !contact.lastMessage.deleted) {
                    Text(
                        modifier = Modifier.alpha(.75f),
                        text = if (!contact.lastMessage.incoming)
                            stringResource(R.string.you) + " " + contact.lastMessage.getMessageTextByAction(
                                context
                            )
                        else
                            contact.lastMessage.getMessageTextByAction(context),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if (contact.contact.hasNewMessage) {
                Icon(
                    modifier = Modifier.weight(1f).alpha(.5f),
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(id = R.string.contact),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
@Preview
fun ContactItemPreview() {
    ContactItem(
        contact = ContactWithLastMessage(
            ContactEntityFactory.createContact(
                address = "12345-5678-5678-12345",
                name = "John",
                publicKey = "public-key",
                guardHostname = "guard-hostname",
                guardAddress = "guard-address",
            ), MessageEntityFactory.createOutgoing(
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
fun ContactItemSelectedPreview()
{
    ContactItem(
        contact = ContactWithLastMessage(
            ContactEntityFactory.createContact(
                address = "12345-5678-5678-12345",
                name = "John",
                publicKey = "public-key",
                guardHostname = "guard-hostname",
                guardAddress = "guard-address",
            ), MessageEntityFactory.createIncoming(
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
