package com.example.enigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.ui.screens.common.ItemsList
import com.example.enigma.util.DatabaseRequestState

@Composable
fun ContactsContent(
    modifier: Modifier = Modifier,
    contacts: DatabaseRequestState<List<ContactEntity>>,
    searchActivated: Boolean,
    searchedContacts: DatabaseRequestState<List<ContactEntity>>,
    isSelectionMode: Boolean,
    selectedContacts: List<ContactEntity>,
    onItemSelected: (ContactEntity) -> Unit,
    onItemDeselected: (ContactEntity) -> Unit,
    navigateToChatScreen: (chatId: String) -> Unit
) {

    val contactsToDisplay = if(searchActivated)
    {
        searchedContacts
    } else
    {
        contacts
    }

    if(contactsToDisplay is DatabaseRequestState.Success)
    {
        if(contactsToDisplay.data.isEmpty())
        {
            EmptyContent(
                modifier = modifier
            )
        } else {
            ItemsList(
                modifier = modifier,
                items = contactsToDisplay.data,
                listItem = { contact, isSelected ->
                    ContactItem(
                       onItemSelected = onItemSelected,
                       onItemDeselected = onItemDeselected,
                       onClick = {
                           navigateToChatScreen(contact.address)
                       },
                       contact = contact,
                       isSelectionMode = isSelectionMode,
                       isSelected = isSelected
                    )
                },
                selectedItems = selectedContacts,
                itemEqualityChecker = { c1, c2 -> c1.address == c2.address},
                itemKeyProvider = { c -> c.address }
            )
        }
    }
}

@Preview
@Composable
fun ContactsContentPreview()
{
    ContactsContent(
        contacts = DatabaseRequestState.Success(
            listOf(
                ContactEntity(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    hasNewMessage = true
                ),
                ContactEntity(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    hasNewMessage = false
                )
            )
        ),
        searchActivated = false,
        searchedContacts = DatabaseRequestState.Success(listOf()),
        isSelectionMode = true,
        selectedContacts = listOf(ContactEntity(
            address = "123",
            name = "John",
            publicKey = "",
            guardHostname = "",
            hasNewMessage = true
        )),
        onItemSelected = { },
        onItemDeselected = { },
        navigateToChatScreen = {}
    )
}
