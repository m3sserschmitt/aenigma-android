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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.data.database.ContactWithConversationPreview
import ro.aenigma.models.enums.ContactType
import ro.aenigma.ui.screens.common.selectable
import java.time.ZonedDateTime

@Composable
fun ContactItem(
    onItemSelected: (ContactWithConversationPreview) -> Unit,
    onItemDeselected: (ContactWithConversationPreview) -> Unit,
    onClick: () -> Unit,
    contact: ContactWithConversationPreview,
    isSelectionMode: Boolean,
    isSelected: Boolean
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
            ).fillMaxWidth().height(64.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(isSelectionMode)
            {
                if(isSelected)
                {
                    Icon(
                        modifier = Modifier.alpha(.5f),
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                else
                {
                    Icon(
                        modifier = Modifier.alpha(.5f),
                        painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            Icon(
                modifier = Modifier.weight(1f).fillMaxSize().alpha(.75f),
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(
                    id = R.string.contact
                ),
                tint = MaterialTheme.colorScheme.onBackground
            )

            val contactNameTextWeight = if(contact.hasNewMessage) 8f else 9f

            Column(
                modifier = Modifier
                    .weight(contactNameTextWeight)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                if(contact.lastMessageText != null)
                {
                    Text(
                        modifier = Modifier.alpha(.75f),
                        text = if(contact.lastMessageIncoming != null && !contact.lastMessageIncoming)
                            stringResource(R.string.you) + " " + contact.lastMessageText
                        else
                            contact.lastMessageText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            if(contact.hasNewMessage)
            {
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
fun ContactItemPreview()
{
    ContactItem(
        contact = ContactWithConversationPreview(
            address = "12345-5678-5678-12345",
            name = "John",
            publicKey = "public-key",
            guardHostname = "guard-hostname",
            guardAddress = "guard-address",
            hasNewMessage = true,
            lastMessageText = "Hey, how are you?",
            lastMessageIncoming = false,
            lastSynchronized = ZonedDateTime.now(),
            type = ContactType.CONTACT,
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
        contact = ContactWithConversationPreview(
            address = "12345-5678-5678-12345",
            name = "John",
            publicKey = "public-key",
            guardHostname = "guard-hostname",
            guardAddress = "guard-address",
            hasNewMessage = true,
            type = ContactType.CONTACT,
            lastMessageText = "Hey, how are you?",
            lastSynchronized = ZonedDateTime.now()
        ),
        isSelectionMode = true,
        isSelected = true,
        onClick = {},
        onItemSelected = {},
        onItemDeselected = {}
    )
}
