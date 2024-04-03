package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.util.DatabaseRequestState

@Composable
fun ContactsListContent(
    modifier: Modifier = Modifier,
    contacts: DatabaseRequestState<List<ContactEntity>>,
    navigateToChatScreen: (chatId: String) -> Unit
)
{
    if(contacts is DatabaseRequestState.Success)
    {
        if(contacts.data.isEmpty())
        {
            EmptyContent(
                modifier = modifier
            )
        } else {
            DisplayContacts(
                modifier = modifier,
                contacts = contacts.data,
                navigateToChatScreen = navigateToChatScreen
            )
        }
    }
}

@Composable
fun DisplayContacts(
    modifier: Modifier = Modifier,
    contacts: List<ContactEntity>,
    navigateToChatScreen: (chatId: String) -> Unit
)
{
    LazyColumn (
        modifier = modifier
    ) {
        items(
            items = contacts,
            key = {
                    contact -> contact.address
            }
        ) {
                contact -> ContactItem(
            contact = contact,
            navigateToChatScreen = {
                navigateToChatScreen(contact.address)
            })
        }
    }
}

@Composable
fun ContactItem(
    contact: ContactEntity,
    navigateToChatScreen: (chatId: String) -> Unit
)
{
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        onClick = {
            navigateToChatScreen(contact.address)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                modifier = Modifier.weight(1f),
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = stringResource(id = R.string.contact),
                // TODO tint = MaterialTheme.colorScheme.contactItemContentColor
            )

            val contactNameTextWeight = if(contact.hasNewMessage) 8f else 9f

            Text(
                modifier = Modifier.weight(contactNameTextWeight),
                text = contact.name,
                // TODO color = MaterialTheme.colorScheme.contactItemContentColor,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            if(contact.hasNewMessage)
            {
                Icon(
                    modifier = Modifier.weight(1f),
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(id = R.string.contact),
                    // TODO tint = MaterialTheme.colorScheme.contactItemContentColor
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
            "12345-5678-5678-12345",
            "John",
            "public-key",
            "guard-hotname",
            true
        ), navigateToChatScreen = {}
    )
}
