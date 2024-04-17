package com.example.enigma.ui.screens.contacts

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.ui.screens.common.ExitSelectionMode
import com.example.enigma.ui.screens.common.RenameContactDialog
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun ContactsScreen(
    navigateToChatScreen: (String) -> Unit,
    navigateToAddContactScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(key1 = true)
    {
        mainViewModel.loadContacts()
    }

    val allContacts by mainViewModel.allContacts.collectAsState()
    val searchedContacts by mainViewModel.searchedContacts.collectAsState()

    ContactsScreen(
        contacts = allContacts,
        searchedContacts = searchedContacts,
        onSearchTriggered = {
            mainViewModel.resetSearchResultResult()
        },
        onSearch = {
            searchQuery ->
            mainViewModel.searchContacts(
                searchQuery = searchQuery
            )
        },
        navigateToChatScreen = navigateToChatScreen,
        onDeleteSelectedItems = {
            contactsToDelete -> mainViewModel.deleteContacts(contactsToDelete)
        },
        navigateToAddContactScreen = navigateToAddContactScreen,
        onContactRenamed = {
            contact, newName -> mainViewModel.renameContact(contact, newName)
        }
    )
}

@Composable
fun ContactsScreen(
    contacts: DatabaseRequestState<List<ContactEntity>>,
    searchedContacts: DatabaseRequestState<List<ContactEntity>>,
    onSearchTriggered: () -> Unit,
    onSearch: (String) -> Unit,
    onDeleteSelectedItems: (List<ContactEntity>) -> Unit,
    onContactRenamed: (ContactEntity, String) -> Unit,
    navigateToAddContactScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit
) {
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var deleteContactsConfirmationVisible by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactEntity>() }

    LaunchedEffect(key1 = contacts)
    {
        if(contacts is DatabaseRequestState.Success)
        {
            selectedItems.removeAll { item -> !contacts.data.contains(item) }
        }
    }

    DeleteSelectedContactsDialog(
        visible = deleteContactsConfirmationVisible,
        onConfirmClicked = {
            deleteContactsConfirmationVisible = false
            onDeleteSelectedItems(selectedItems)
        },
        onDismissClicked = {
            deleteContactsConfirmationVisible = false
        }
    )

    RenameContactDialog(
        visible = renameContactDialogVisible,
        contacts = contacts,
        onContactRenamed = {
            newContactName -> if(selectedItems.size == 1)
            {
                onContactRenamed(selectedItems.single(), newContactName)
            }
            renameContactDialogVisible = false
        },
        onDismiss = {
            renameContactDialogVisible = false
        }
    )

    BackHandler(
        enabled = isSearchMode
    ) {
        isSearchMode = false
    }

    ExitSelectionMode(
        isSelectionMode = isSelectionMode,
        selectedItemsCount = selectedItems.size,
        onSelectionModeExited = {
            isSelectionMode = false
        }
    )

    Scaffold(
        topBar = {
            ContactsAppBar(
                isSearchMode = isSearchMode,
                onSearchTriggered = {
                    isSearchMode = true
                    onSearchTriggered()
                },
                onSearchClicked = {
                    searchQuery -> onSearch(searchQuery)
                },
                onSearchDeactivated = {
                    isSearchMode = false
                },
                onSelectionModeExited = {
                    selectedItems.clear()
                },
                isSelectionMode = isSelectionMode,
                selectedItemsCount = selectedItems.size,
                onDeleteSelectedItemsClicked = {
                    deleteContactsConfirmationVisible = true
                },
                onRenameSelectedItemClicked = {
                    renameContactDialogVisible = true
                }
            )
        },
        content = { paddingValues ->
            ContactsContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                contacts = contacts,
                searchActivated = isSearchMode,
                searchedContacts = searchedContacts,
                navigateToChatScreen = navigateToChatScreen,
                onItemSelected = { selectedContact ->
                    if(!isSelectionMode)
                    {
                        isSelectionMode = true
                    }

                    selectedItems.add(selectedContact)
                },
                onItemDeselected = {
                        deselectedContact -> selectedItems.remove(deselectedContact)
                },
                isSelectionMode = isSelectionMode,
                selectedContacts = selectedItems
            )
        },
        floatingActionButton = {
            ContactsFab(
                onFabClicked = navigateToAddContactScreen
            )
        }
    )
}

@Composable
fun ContactsFab(
    onFabClicked: () -> Unit
) {
    FloatingActionButton(
        onClick = { onFabClicked() },
    ) {
        Icon (
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource (
                id = R.string.contacts_floating_button_content_description
            ),
            tint = Color.White
        )
    }
}

@Preview
@Composable
fun ContactsScreenPreview()
{
    ContactsScreen(
        onContactRenamed = { _, _ -> },
        onDeleteSelectedItems = {},
        onSearch = {},
        onSearchTriggered = {},
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
        searchedContacts = DatabaseRequestState.Success(listOf()),
        navigateToChatScreen = {},
        navigateToAddContactScreen = {}
    )
}