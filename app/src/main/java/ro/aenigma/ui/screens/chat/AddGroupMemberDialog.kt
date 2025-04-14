package ro.aenigma.ui.screens.chat

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.data.database.ContactEntity
import ro.aenigma.data.database.ContactWithGroup
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.ui.screens.common.DialogContentTemplate
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.contacts.ContactItem
import ro.aenigma.util.RequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGroupMemberDialog(
    action: MessageType,
    visible: Boolean,
    contactWithGroup: RequestState<ContactWithGroup>,
    allContacts: RequestState<List<ContactEntity>>,
    onDismissClicked: () -> Unit,
    onConfirmClicked: (List<String>) -> Unit
) {
    if (visible && allContacts is RequestState.Success && contactWithGroup is RequestState.Success) {
        val add = action == MessageType.GROUP_MEMBER_ADD
        val selectedItems = remember { mutableStateListOf<ContactWithLastMessage>() }
        val memberAddresses = remember(contactWithGroup.data.group?.groupData?.members) {
            val items = hashSetOf<String>()
            contactWithGroup.data.group?.groupData?.members?.mapNotNullTo(items) { item -> item.address }
                ?: hashSetOf()
        }
        val items = remember(allContacts, memberAddresses, add) {
            allContacts.data.mapNotNull { item ->
                if (!memberAddresses.contains(item.address) == add && item.type == ContactType.CONTACT)
                    ContactWithLastMessage(item, null) else null
            }
        }
        val title = when {
            add && items.isNotEmpty() -> stringResource(id = R.string.select_contacts_to_add)
            !add && items.isNotEmpty() -> stringResource(id = R.string.select_members_to_remove)
            else -> stringResource(id = R.string.no_contacts_found)
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
                positiveButtonVisible = items.isNotEmpty(),
                content = {
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
            )
        }
    }
}

@Preview
@Composable
fun AddGroupMemberDialogPreview() {
    AddGroupMemberDialog(
        action = MessageType.GROUP_MEMBER_ADD,
        visible = true,
        contactWithGroup = RequestState.Idle,
        allContacts = RequestState.Success(
            listOf(
                ContactEntityFactory.createContact(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                ),
                ContactEntityFactory.createContact(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                )
            )
        ),
        onConfirmClicked = { },
        onDismissClicked = { }
    )
}
