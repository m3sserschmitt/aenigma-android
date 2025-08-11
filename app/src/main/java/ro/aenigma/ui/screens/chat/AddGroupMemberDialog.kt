package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.extensions.ContactEntityExtensions.toDto
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ContactDtoExtensions.toEntity
import ro.aenigma.models.extensions.ExportedContactDataExtensions.toContactDto
import ro.aenigma.ui.screens.common.DialogContentTemplate
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.contacts.ContactItem
import ro.aenigma.util.RequestState

@Composable
private fun getContactsList(
    action: MessageType,
    searchQuery: String,
    existentMembers: List<ExportedContactData>?,
    contacts: RequestState<List<ContactDto>>
): List<ContactWithLastMessage> {
    val memberAddresses = remember(key1 = action, key2 = existentMembers) {
        if (action == MessageType.GROUP_MEMBER_ADD) {
            val existentItemsSet = hashSetOf<String>()
            existentMembers?.mapNotNullTo(existentItemsSet) { item -> item.address } ?: hashSetOf()
        } else {
            hashSetOf()
        }
    }
    return remember(action, existentMembers, contacts, searchQuery) {
        when (action) {
            MessageType.GROUP_MEMBER_ADD -> {
                if (contacts is RequestState.Success) {
                    contacts.data.filter { contact ->
                        contact.type == ContactType.CONTACT && !memberAddresses.contains(
                            contact.address
                        )
                    }
                } else {
                    listOf()
                }
            }

            MessageType.GROUP_MEMBER_REMOVE -> {
                existentMembers?.mapNotNull { item ->
                    if (item.name?.contains(searchQuery, ignoreCase = true) == true) {
                        item.toContactDto()
                    } else {
                        null
                    }
                } ?: listOf()
            }

            else -> {
                listOf()
            }
        }.map { item -> ContactWithLastMessage(item.toEntity(), null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupMemberDialog(
    action: MessageType,
    visible: Boolean,
    contactWithGroup: RequestState<ContactWithGroup>,
    contacts: RequestState<List<ContactDto>>,
    onSearchQueryChanged: (String) -> Unit,
    onDismissClicked: () -> Unit,
    onConfirmClicked: (List<String>) -> Unit
) {
    if (!visible || contactWithGroup !is RequestState.Success) {
        return
    }

    val add = action == MessageType.GROUP_MEMBER_ADD
    var searchQuery by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateListOf<ContactWithLastMessage>() }
    val items = getContactsList(
        action = action,
        existentMembers = contactWithGroup.data.group?.groupData?.members,
        contacts = contacts,
        searchQuery = searchQuery
    )
    val isNotEmpty = items.isNotEmpty()
    val context = LocalContext.current
    val title = remember(add) {
        when {
            add && isNotEmpty -> context.getString(R.string.select_contacts_to_add)
            !add && isNotEmpty -> context.getString(R.string.select_members_to_remove)
            else -> context.getString(R.string.no_contacts_found)
        }
    }

    BasicAlertDialog(onDismissRequest = onDismissClicked) {
        DialogContentTemplate(
            title = title,
            body = "",
            dismissible = true,
            onNegativeButtonClicked = onDismissClicked,
            onPositiveButtonClicked = {
                onConfirmClicked(selectedItems.map { item -> item.contact.address })
            },
            positiveButtonVisible = isNotEmpty,
            content = {
                Column {
                    if (isNotEmpty) {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = searchQuery,
                            onValueChange = { text ->
                                searchQuery = text
                                if(action == MessageType.GROUP_MEMBER_ADD) {
                                    onSearchQueryChanged(searchQuery)
                                }
                            },
                            placeholder = {
                                Text(
                                    modifier = Modifier.alpha(0.25f),
                                    text = stringResource(id = R.string.search),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors().copy(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                            )
                        )
                        ItemsList(
                            items = items,
                            listItem = { _, entity, isSelected ->
                                ContactItem(
                                    onItemSelected = { item ->
                                        selectedItems.add(item)
                                    },
                                    onItemDeselected = { item ->
                                        selectedItems.remove(item)
                                    },
                                    onClick = { },
                                    contact = entity,
                                    isSelectionMode = true,
                                    isSelected = isSelected
                                )
                            },
                            selectedItems = selectedItems,
                            itemKeyProvider = { c -> c.contact.address }
                        )
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun AddGroupMemberDialogPreview() {
    AddGroupMemberDialog(
        action = MessageType.GROUP_MEMBER_ADD,
        visible = true,
        contactWithGroup = RequestState.Idle,
        contacts = RequestState.Success(
            listOf(
                ContactEntityFactory.createContact(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                ).toDto(),
                ContactEntityFactory.createContact(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                ).toDto()
            )
        ),
        onConfirmClicked = { },
        onSearchQueryChanged = { },
        onDismissClicked = { }
    )
}
