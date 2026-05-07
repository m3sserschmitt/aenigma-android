package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ContactDto
import ro.aenigma.models.ContactWithGroupDto
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.extensions.ExportedContactDataExtensions.toContactDto
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.ui.screens.common.DialogContentTemplate
import ro.aenigma.ui.screens.common.IndeterminateCircularIndicator
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.contacts.ContactItem
import ro.aenigma.util.Constants.Companion.BROADCAST_CONTACT_ADDRESS
import ro.aenigma.util.RequestState

@Composable
private fun rememberContactsList(
    action: MessageType,
    searchQuery: String,
    contactWithGroup: RequestState<ContactWithGroupDto>,
    contacts: RequestState<List<ContactDto>>
): RequestState<List<ContactDto>> {
    return remember(action, contactWithGroup, contacts, searchQuery) {
        when (action) {
            MessageType.GROUP_MEMBER_ADD -> {
                when (contacts) {
                    is RequestState.Success -> {
                        RequestState.Success(
                            data = contacts.data.filter { contact ->
                                contact.type == ContactType.CONTACT && contact.name?.contains(
                                    searchQuery,
                                    ignoreCase = true
                                ) == true && contact.address != BROADCAST_CONTACT_ADDRESS
                            }
                        )
                    }

                    is RequestState.Idle,
                    is RequestState.Loading -> {
                        RequestState.Loading
                    }

                    is RequestState.Error -> {
                        RequestState.Error(contacts.error)
                    }
                }
            }

            MessageType.GROUP_MEMBER_REMOVE -> {
                when (contactWithGroup) {
                    is RequestState.Success -> {
                        RequestState.Success(
                            data = contactWithGroup.data.group?.groupData?.members?.mapNotNull { item ->
                                if (item.name?.contains(searchQuery, ignoreCase = true) == true
                                    && contactWithGroup.data.group.groupData.admins?.contains(item.address) != true
                                ) {
                                    item.toContactDto()
                                } else {
                                    null
                                }
                            } ?: listOf()
                        )
                    }

                    is RequestState.Loading,
                    is RequestState.Idle -> {
                        RequestState.Loading
                    }

                    is RequestState.Error -> {
                        RequestState.Error(Exception())
                    }
                }
            }

            else -> {
                RequestState.Success(listOf())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupMemberDialog(
    action: MessageType,
    visible: Boolean,
    contactWithGroup: RequestState<ContactWithGroupDto>,
    contacts: RequestState<List<ContactDto>>,
    onSearchQueryChanged: (String) -> Unit,
    onDismissClicked: () -> Unit,
    onConfirmClicked: (List<ContactDto>) -> Unit
) {
    if (!visible) {
        return
    }

    var searchQuery by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateMapOf<String, ContactDto>() }
    val items = rememberContactsList(
        action = action,
        contactWithGroup = contactWithGroup,
        contacts = contacts,
        searchQuery = searchQuery
    )
    val isNotEmpty =
        remember(key1 = items) { items is RequestState.Success && items.data.isNotEmpty() }

    BasicAlertDialog(onDismissRequest = onDismissClicked) {
        DialogContentTemplate(
            title = when (action == MessageType.GROUP_MEMBER_ADD) {
                true -> stringResource(id = R.string.add_channel_member)
                false -> stringResource(id = R.string.remove_channel_member)
            },
            body = "",
            dismissible = true,
            onNegativeButtonClicked = onDismissClicked,
            onPositiveButtonClicked = {
                onConfirmClicked(selectedItems.values.toList())
            },
            positiveButtonVisible = isNotEmpty && selectedItems.isNotEmpty(),
            content = {
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = searchQuery,
                    onValueChange = { text ->
                        searchQuery = text
                        if (action == MessageType.GROUP_MEMBER_ADD) {
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
                when (items) {
                    is RequestState.Success -> {
                        if (isNotEmpty) {
                            ItemsList(
                                items = items.data,
                                listItem = { _, entity, isSelected ->
                                    ContactItem(
                                        onItemSelected = { item ->
                                            selectedItems[item.address] = item
                                        },
                                        onItemDeselected = { item ->
                                            selectedItems.remove(item.address)
                                        },
                                        onClick = { },
                                        contact = entity,
                                        isSelectionMode = true,
                                        isSelected = isSelected
                                    )
                                },
                                selectedItems = selectedItems,
                                itemKeySelector = { c -> c.address }
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = R.drawable.ic_people
                                    ),
                                    contentDescription = stringResource(id = R.string.no_contacts_found),
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    modifier = Modifier.padding(top = 4.dp),
                                    text = stringResource(
                                        id = R.string.no_contacts_found
                                    ),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    is RequestState.Idle,
                    is RequestState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IndeterminateCircularIndicator(
                                visible = true,
                                size = 24.dp,
                                text = stringResource(id = R.string.loading),
                                color = MaterialTheme.colorScheme.onBackground,
                                textStyle = MaterialTheme.typography.bodySmall,
                                textColor = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    is RequestState.Error -> {

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
                ContactDtoFactory.createContact(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                ),
                ContactDtoFactory.createContact(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                )
            )
        ),
        onConfirmClicked = { },
        onSearchQueryChanged = { },
        onDismissClicked = { }
    )
}
