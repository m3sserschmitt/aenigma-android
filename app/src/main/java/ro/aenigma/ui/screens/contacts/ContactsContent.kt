package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.models.ContactWithLastMessageDto
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.util.RequestState

@Composable
fun ContactsContent(
    modifier: Modifier = Modifier,
    contacts: RequestState<List<ContactWithLastMessageDto>>,
    isSearchMode: Boolean,
    isSelectionMode: Boolean,
    selectedContacts: Map<String, ContactWithLastMessageDto>,
    onItemSelected: (ContactWithLastMessageDto) -> Unit,
    onItemDeselected: (ContactWithLastMessageDto) -> Unit,
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
                        ContactWithChatPreviewItem(
                            onItemSelected = onItemSelected,
                            onItemDeselected = onItemDeselected,
                            onClick = { item -> navigateToChatScreen(item.contact.address) },
                            contact = contact,
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected
                        )
                    },
                    selectedItems = selectedContacts,
                    itemKeySelector = { c -> c.contact.address }
                )

            } else {
                if(isSearchMode)
                {
                    EmptySearchResult(modifier = modifier)
                } else
                {
                    EmptyContactsScreen(modifier = modifier)
                }
            }
        }
        is RequestState.Error -> GenericErrorScreen(modifier = modifier)
        is RequestState.Loading -> LoadingScreen(modifier = modifier)
        is RequestState.Idle -> { }
    }
}

@Preview
@Composable
fun ContactsContentPreview() {
    ContactsContent(
        contacts = RequestState.Success(
            listOf(
                ContactWithLastMessageDto(
                    ContactDtoFactory.createContact(
                        address = "123",
                        name = "John",
                        publicKey = "",
                        guardHostname = "",
                        guardAddress = "",
                    ), null
                ),
                ContactWithLastMessageDto(
                    ContactDtoFactory.createContact(
                        address = "124",
                        name = "Paul",
                        publicKey = "",
                        guardHostname = "",
                        guardAddress = "",
                    ), null
                )
            )
        ),
        isSearchMode = false,
        isSelectionMode = true,
        selectedContacts = mapOf(),
        onItemSelected = { },
        onItemDeselected = { },
        navigateToChatScreen = {}
    )
}
