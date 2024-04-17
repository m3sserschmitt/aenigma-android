package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.ui.screens.common.selectable

@Composable
fun ContactItem(
    onItemSelected: (ContactEntity) -> Unit,
    onItemDeselected: (ContactEntity) -> Unit,
    onClick: () -> Unit,
    contact: ContactEntity,
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
            ).fillMaxWidth().height(56.dp),
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
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
                else
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                }
            }

            Icon(
                modifier = Modifier
                    .weight(1f)
                    .size(32.dp),
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(
                    id = R.string.contact
                ),
            )

            val contactNameTextWeight = if(contact.hasNewMessage) 8f else 9f

            Text(
                modifier = Modifier
                    .weight(contactNameTextWeight),
                text = contact.name,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                maxLines = 1
            )
            if(contact.hasNewMessage)
            {
                Icon(
                    modifier = Modifier.weight(1f),
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(id = R.string.contact),
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
        contact = ContactEntity(
            address = "12345-5678-5678-12345",
            name = "John",
            publicKey = "public-key",
            guardHostname = "guard-hostname",
            hasNewMessage = true
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
        contact = ContactEntity(
            address = "12345-5678-5678-12345",
            name = "John",
            publicKey = "public-key",
            guardHostname = "guard-hostname",
            hasNewMessage = true
        ),
        isSelectionMode = true,
        isSelected = true,
        onClick = {},
        onItemSelected = {},
        onItemDeselected = {}
    )
}
