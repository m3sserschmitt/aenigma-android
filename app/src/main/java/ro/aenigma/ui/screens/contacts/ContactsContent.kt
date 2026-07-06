/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
