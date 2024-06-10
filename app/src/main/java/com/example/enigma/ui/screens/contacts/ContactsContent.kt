package com.example.enigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.data.database.ContactWithConversationPreview
import com.example.enigma.ui.screens.common.ErrorScreen
import com.example.enigma.ui.screens.common.ItemsList
import com.example.enigma.ui.screens.common.LoadingScreen
import com.example.enigma.util.DatabaseRequestState

@Composable
fun ContactsContent(
    modifier: Modifier = Modifier,
    contacts: DatabaseRequestState<List<ContactWithConversationPreview>>,
    isSearchMode: Boolean,
    isSelectionMode: Boolean,
    selectedContacts: List<ContactWithConversationPreview>,
    onItemSelected: (ContactWithConversationPreview) -> Unit,
    onItemDeselected: (ContactWithConversationPreview) -> Unit,
    navigateToChatScreen: (chatId: String) -> Unit
) {
    when(contacts)
    {
        is DatabaseRequestState.Success -> {
            if(contacts.data.isNotEmpty())
            {
                ItemsList(
                    modifier = modifier,
                    items = contacts.data,
                    listItem = { _, contact, isSelected ->
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
                    itemKeyProvider = { c -> c.address }
                )

            } else {
                if(isSearchMode)
                {
                    EmptySearchResult(modifier)
                } else
                {
                    EmptyContactsScreen(
                        modifier = modifier
                    )
                }
            }
        }
        is DatabaseRequestState.Error -> ErrorScreen(modifier)
        is DatabaseRequestState.Loading -> LoadingScreen(modifier)
        is DatabaseRequestState.Idle -> { }
    }
}

@Preview
@Composable
fun ContactsContentPreview()
{
    ContactsContent(
        contacts = DatabaseRequestState.Success(
            listOf(
                ContactWithConversationPreview(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    hasNewMessage = true
                ),
                ContactWithConversationPreview(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    hasNewMessage = false
                )
            )
        ),
        isSearchMode = false,
        isSelectionMode = true,
        selectedContacts = listOf(ContactWithConversationPreview(
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
