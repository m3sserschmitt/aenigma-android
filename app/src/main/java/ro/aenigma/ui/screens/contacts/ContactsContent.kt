package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.models.enums.ContactType
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.util.RequestState
import java.time.ZonedDateTime

@Composable
fun ContactsContent(
    modifier: Modifier = Modifier,
    contacts: RequestState<List<ContactWithLastMessage>>,
    isSearchMode: Boolean,
    isSelectionMode: Boolean,
    selectedContacts: List<ContactWithLastMessage>,
    onItemSelected: (ContactWithLastMessage) -> Unit,
    onItemDeselected: (ContactWithLastMessage) -> Unit,
    navigateToChatScreen: (chatId: String) -> Unit
) {
    when(contacts)
    {
        is RequestState.Success -> {
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
                                navigateToChatScreen(contact.contact.address)
                            },
                            contact = contact,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    },
                    selectedItems = selectedContacts,
                    itemKeyProvider = { c -> c.contact.address }
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
        is RequestState.Error -> GenericErrorScreen(modifier)
        is RequestState.Loading -> LoadingScreen(modifier)
        is RequestState.Idle -> { }
    }
}

@Preview
@Composable
fun ContactsContentPreview() {
    ContactsContent(
        contacts = RequestState.Success(
            listOf(
                ContactWithLastMessage(
                    ContactEntity(
                        address = "123",
                        name = "John",
                        publicKey = "",
                        guardHostname = "",
                        guardAddress = "",
                        hasNewMessage = true,
                        type = ContactType.CONTACT,
                        lastSynchronized = ZonedDateTime.now()
                    ), null
                ),
                ContactWithLastMessage(
                    ContactEntity(
                        address = "124",
                        name = "Paul",
                        publicKey = "",
                        guardHostname = "",
                        guardAddress = "",
                        hasNewMessage = false,
                        type = ContactType.CONTACT,
                        lastSynchronized = ZonedDateTime.now()
                    ), null
                )
            )
        ),
        isSearchMode = false,
        isSelectionMode = true,
        selectedContacts = listOf(
            ContactWithLastMessage(
                ContactEntity(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                    hasNewMessage = true,
                    type = ContactType.CONTACT,
                    lastSynchronized = ZonedDateTime.now()
                ), null
            )
        ),
        onItemSelected = { },
        onItemDeselected = { },
        navigateToChatScreen = {}
    )
}
