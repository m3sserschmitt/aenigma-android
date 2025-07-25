package ro.aenigma.ui.screens.contacts

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.data.database.ContactWithLastMessage
import ro.aenigma.data.database.factories.ContactEntityFactory
import ro.aenigma.data.database.factories.MessageEntityFactory
import ro.aenigma.services.SignalRStatus
import ro.aenigma.models.SharedData
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.ui.screens.common.SaveNewContactDialog
import ro.aenigma.ui.screens.common.ConnectionStatusSnackBar
import ro.aenigma.ui.screens.common.ExitSelectionMode
import ro.aenigma.ui.screens.common.NotificationsPermissionRequiredDialog
import ro.aenigma.ui.screens.common.CheckNotificationsPermission
import ro.aenigma.ui.screens.common.LoadingDialog
import ro.aenigma.ui.screens.common.RenameContactDialog
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.ContextExtensions.openApplicationDetails
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel
import java.time.ZonedDateTime

@Composable
fun ContactsScreen(
    navigateToChatScreen: (String) -> Unit,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToAboutScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    val allContacts by mainViewModel.allContacts.collectAsState()
    val connectionStatus by mainViewModel.clientStatus.collectAsState()
    val notificationsAllowed by mainViewModel.notificationsAllowed.collectAsState()
    val userName by mainViewModel.userName.collectAsState()
    val sharedDataRequest by mainViewModel.sharedDataRequest.collectAsState()
    val useTor by mainViewModel.useTor.collectAsState()

    ContactsScreen(
        connectionStatus = connectionStatus,
        contacts = allContacts,
        sharedDataRequest = sharedDataRequest,
        notificationsAllowed = notificationsAllowed,
        nameDialogVisible = userName.isBlank(),
        useTor = useTor,
        useTorChanged = { useTor -> mainViewModel.useTorChanged(useTor) },
        onNotificationsPreferenceChanged = {
            allowed -> mainViewModel.saveNotificationsPreference(allowed)
        },
        onRetryConnection = { mainViewModel.retryClientConnection() },
        onSearch = { searchQuery -> mainViewModel.searchContacts(searchQuery) },
        navigateToChatScreen = navigateToChatScreen,
        onDeleteSelectedItems = { contactsToDelete -> mainViewModel.deleteContacts(contactsToDelete) },
        navigateToAddContactScreen = navigateToAddContactScreen,
        navigateToAboutScreen = navigateToAboutScreen,
        onContactRenamed = { contactToBeRenamed, newName ->
            mainViewModel.renameContact(contactToBeRenamed, newName)
        },
        onNewContactNameChanged =  { newValue -> mainViewModel.validateNewContactName(newValue) },
        onContactSaved = { name -> mainViewModel.saveNewContact(name) },
        onGroupCreated = { selectedItems, name -> mainViewModel.createGroup(selectedItems, name) },
        onNameConfirmed = { nameValue -> mainViewModel.setupName(nameValue) },
        onResetUserNameClicked = { mainViewModel.resetUserName() },
        onContactSaveDismissed = { mainViewModel.resetContactChanges() }
    )
}

@Composable
fun ContactsScreen(
    connectionStatus: SignalRStatus,
    contacts: RequestState<List<ContactWithLastMessage>>,
    sharedDataRequest: RequestState<SharedData>,
    notificationsAllowed: Boolean,
    nameDialogVisible: Boolean,
    useTor: Boolean,
    useTorChanged: (Boolean) -> Unit,
    onNotificationsPreferenceChanged: (Boolean) -> Unit,
    onRetryConnection: () -> Unit,
    onSearch: (String) -> Unit,
    onDeleteSelectedItems: (List<ContactWithLastMessage>) -> Unit,
    onContactRenamed: (ContactWithLastMessage, String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onContactSaved: (String) -> Unit,
    onGroupCreated: (List<ContactWithLastMessage>, String) -> Unit,
    onContactSaveDismissed: () -> Unit,
    onNameConfirmed: (String) -> Unit,
    onResetUserNameClicked: () -> Unit,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToAboutScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit
) {
    var createGroupDialogVisible by remember { mutableStateOf(false) }
    var permissionRequiredDialogVisible by remember { mutableStateOf(false) }
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var deleteContactsConfirmationVisible by remember { mutableStateOf(false) }
    var getContactDataLoadingDialogVisible by remember { mutableStateOf(true) }
    var saveContactDialogVisible by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactWithLastMessage>() }
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = contacts)
    {
        if (contacts is RequestState.Success && !isSearchMode) {
            selectedItems.removeAll { item -> !contacts.data.contains(item) }
        }
    }

    LaunchedEffect(key1 = isSearchMode) {
        if (!isSearchMode) {
            onSearch("")
        }
    }

    LaunchedEffect(key1 = sharedDataRequest) {
        if (sharedDataRequest is RequestState.Error) {
            Toast.makeText(context, "Request completed with errors.", Toast.LENGTH_SHORT).show()
        }
    }

    CheckNotificationsPermission(
        onPermissionGranted = { granted ->
            permissionRequiredDialogVisible = !granted && notificationsAllowed
            if (granted) onNotificationsPreferenceChanged(true)
        }
    )

    NotificationsPermissionRequiredDialog(
        visible = permissionRequiredDialogVisible,
        onPositiveButtonClicked = {
            permissionRequiredDialogVisible = false
            context.openApplicationDetails()
        },
        onNegativeButtonClicked = { rememberDecision ->
            if (rememberDecision) onNotificationsPreferenceChanged(false)
            permissionRequiredDialogVisible = false
        }
    )

    DeleteSelectedContactsDialog(
        visible = deleteContactsConfirmationVisible,
        onConfirmClicked = {
            deleteContactsConfirmationVisible = false
            onDeleteSelectedItems(selectedItems.toList())
            selectedItems.clear()
        },
        onDismissClicked = {
            deleteContactsConfirmationVisible = false
        }
    )

    RenameContactDialog(
        visible = renameContactDialogVisible,
        onNewContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            val contact = selectedItems.singleOrNull()
            if (contact != null) {
                onContactRenamed(contact, name)
            }
            renameContactDialogVisible = false
            isSelectionMode = false
            selectedItems.clear()
        },
        onDismiss = {
            renameContactDialogVisible = false
        }
    )

    LoadingDialog(
        visible = getContactDataLoadingDialogVisible,
        state = sharedDataRequest,
        onConfirmButtonClicked = {
            if (sharedDataRequest is RequestState.Error) {
                onContactSaveDismissed()
            } else {
                saveContactDialogVisible = true
            }
            getContactDataLoadingDialogVisible = false
        }
    )

    SaveNewContactDialog(
        visible = sharedDataRequest is RequestState.Success && saveContactDialogVisible,
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            onContactSaved(name)
            saveContactDialogVisible = false
        },
        onDismissClicked = {
            onContactSaveDismissed()
            saveContactDialogVisible = false
        }
    )

    CreateGroupDialog(
        visible = createGroupDialogVisible,
        onTextChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            onGroupCreated(selectedItems.toList(), name)
            createGroupDialogVisible = false
            isSelectionMode = false
            selectedItems.clear()
        },
        onDismissClicked = {
            onContactSaveDismissed()
            createGroupDialogVisible = false
        }
    )

    SetupUserNameDialog(
        visible = nameDialogVisible,
        onConfirmClicked = onNameConfirmed
    )

    BackHandler(
        enabled = isSearchMode || isSelectionMode
    ) {
        if (isSearchMode) {
            isSearchMode = false
        } else if (isSelectionMode) {
            selectedItems.clear()
            isSelectionMode = false
        }
    }

    ExitSelectionMode(
        isSelectionMode = isSelectionMode,
        selectedItemsCount = selectedItems.size,
        onSelectionModeExited = {
            isSelectionMode = false
            selectedItems.clear()
        }
    )

    ConnectionStatusSnackBar(
        message = stringResource(id = R.string.connection_failed),
        actionLabel = stringResource(id = R.string.retry),
        connectionStatus = connectionStatus,
        targetStatus = SignalRStatus.Error.Aborted::class.java,
        snackBarHostState = snackBarHostState,
        onActionPerformed = onRetryConnection
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        topBar = {
            ContactsAppBar(
                connectionStatus = connectionStatus,
                isSearchMode = isSearchMode,
                useTor = useTor,
                useTorChanged = useTorChanged,
                onSearchTriggered = {
                    isSearchMode = true
                },
                onSearchClicked = { searchQuery ->
                    onSearch(searchQuery)
                },
                onSearchModeExited = {
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
                },
                onShareSelectedItemsClicked = {
                    val selectedItem = selectedItems.singleOrNull()?.contact
                    if (selectedItem != null && selectedItem.type == ContactType.CONTACT) {
                        navigateToAddContactScreen(selectedItem.address)
                    } else if (selectedItem != null && selectedItem.type == ContactType.GROUP) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.cannot_share_channels),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                onCreateGroupClicked = {
                    if (selectedItems.any { item -> item.contact.type == ContactType.GROUP }) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.cannot_select_channels_to_create_channel),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        createGroupDialogVisible = true
                    }
                },
                onResetUsernameClicked = onResetUserNameClicked,
                onRetryConnection = onRetryConnection,
                navigateToAboutScreen = navigateToAboutScreen
            )
        },
        content = { paddingValues ->
            ContactsContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                ),
                contacts = contacts,
                isSearchMode = isSearchMode,
                navigateToChatScreen = navigateToChatScreen,
                onItemSelected = { selectedContact ->
                    if (!isSelectionMode) {
                        isSelectionMode = true
                    }

                    selectedItems.add(selectedContact)
                },
                onItemDeselected = { deselectedContact ->
                    selectedItems.remove(deselectedContact)
                },
                isSelectionMode = isSelectionMode,
                selectedContacts = selectedItems
            )
        },
        floatingActionButton = {
            ContactsFab(
                onFabClicked = {
                    navigateToAddContactScreen(null)
                }
            )
        }
    )
}

@Composable
fun ContactsFab(
    onFabClicked: () -> Unit
) {
    FloatingActionButton(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        onClick = { onFabClicked() },
    ) {
        Icon (
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource (
                id = R.string.contacts_floating_button_content_description
            ),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview
@Composable
fun ContactsScreenPreview() {
    ContactsScreen(
        connectionStatus = SignalRStatus.Connected,
        notificationsAllowed = true,
        nameDialogVisible = false,
        useTor = true,
        useTorChanged = { _ -> },
        onNotificationsPreferenceChanged = {},
        onRetryConnection = {},
        onContactRenamed = { _, _ -> },
        onNewContactNameChanged = { true },
        onDeleteSelectedItems = {},
        onSearch = {},
        onGroupCreated = { _, _ -> },
        contacts = RequestState.Success(
            listOf(
                ContactWithLastMessage(
                    ContactEntityFactory.createContact(
                        address = "123",
                        name = "John",
                        publicKey = null,
                        guardHostname = null,
                        guardAddress = null,
                    ), MessageEntityFactory.createOutgoing(
                        chatId = "123",
                        text = "Awesome!",
                        type = MessageType.TEXT,
                        actionFor = null,
                    )
                ),
                ContactWithLastMessage(
                    ContactEntityFactory.createContact(
                        address = "124",
                        name = "Elizabeth",
                        publicKey = null,
                        guardHostname = null,
                        guardAddress = null,
                    ), MessageEntityFactory.createIncoming(
                        chatId = "124",
                        text = "Can't wait to see you tomorrow!",
                        type = MessageType.TEXT,
                        senderAddress = "124",
                        serverUUID = null,
                        actionFor = null,
                        refId = null,
                        dateReceivedOnServer = ZonedDateTime.now()
                    )
                )
            )
        ),
        onResetUserNameClicked = {},
        navigateToChatScreen = {},
        navigateToAddContactScreen = {},
        onContactSaved = {},
        onContactSaveDismissed = {},
        sharedDataRequest = RequestState.Idle,
        navigateToAboutScreen = { },
        onNameConfirmed = {}
    )
}

@Preview
@Composable
fun ContactsScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        ContactsScreenPreview()
    }
}
