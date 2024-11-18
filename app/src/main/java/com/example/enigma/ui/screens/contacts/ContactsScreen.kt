package com.example.enigma.ui.screens.contacts

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.data.database.ContactWithConversationPreview
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.models.SharedData
import com.example.enigma.ui.screens.common.ConnectionStatusSnackBar
import com.example.enigma.ui.screens.common.ExitSelectionMode
import com.example.enigma.ui.screens.common.NotificationsPermissionRequiredDialog
import com.example.enigma.ui.screens.common.CheckNotificationsPermission
import com.example.enigma.ui.screens.common.RenameContactDialog
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.openApplicationDetails
import com.example.enigma.viewmodels.MainViewModel
import java.time.ZonedDateTime

@Composable
fun ContactsScreen(
    navigateToChatScreen: (String) -> Unit,
    navigateToAddContactScreen: (String?) -> Unit,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(key1 = true)
    {
        mainViewModel.loadContacts()
    }

    val allContacts by mainViewModel.allContacts.collectAsState()
    val connectionStatus by mainViewModel.signalRClientStatus.observeAsState(
        initial = SignalRStatus.NotConnected()
    )
    val notificationsAllowed by mainViewModel.notificationsPermissionGranted.collectAsState(
        initial = true
    )
    val sharedDataRequest by mainViewModel.sharedDataRequest.collectAsState()

    ContactsScreen(
        connectionStatus = connectionStatus,
        contacts = allContacts,
        sharedDataRequest = sharedDataRequest,
        notificationsAllowed = notificationsAllowed,
        onNotificationsPreferenceChanged = {
            allowed -> mainViewModel.saveNotificationsPreference(allowed)
        },
        onRetryConnection = {
            mainViewModel.retryClientConnection()
        },
        onSearch = {
            searchQuery -> mainViewModel.searchContacts(searchQuery)
        },
        navigateToChatScreen = navigateToChatScreen,
        onDeleteSelectedItems = {
            contactsToDelete -> mainViewModel.deleteContacts(contactsToDelete)
        },
        navigateToAddContactScreen = navigateToAddContactScreen,
        onContactRenamed = {
            contactToBeRenamed -> mainViewModel.renameContact(contactToBeRenamed)
        },
        onNewContactNameChanged =  {
            newValue -> mainViewModel.setNewContactName(newValue)
        },
        onContactSaved = {
            mainViewModel.saveContactChanges()
        },
        onContactSaveDismissed = {
            mainViewModel.cleanupContactChanges()
        }
    )
}

@Composable
fun ContactsScreen(
    connectionStatus: SignalRStatus,
    contacts: DatabaseRequestState<List<ContactWithConversationPreview>>,
    sharedDataRequest: DatabaseRequestState<SharedData>,
    notificationsAllowed: Boolean,
    onNotificationsPreferenceChanged: (Boolean) -> Unit,
    onRetryConnection: () -> Unit,
    onSearch: (String) -> Unit,
    onDeleteSelectedItems: (List<ContactWithConversationPreview>) -> Unit,
    onContactRenamed: (ContactWithConversationPreview) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onContactSaved: () -> Unit,
    onContactSaveDismissed: () -> Unit,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToChatScreen: (String) -> Unit
) {
    var permissionRequiredDialogVisible by remember { mutableStateOf(false) }
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var deleteContactsConfirmationVisible by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedItems = remember { mutableStateListOf<ContactWithConversationPreview>() }
    val snackBarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(key1 = contacts)
    {
        if(contacts is DatabaseRequestState.Success)
        {
            selectedItems.removeAll { item -> !contacts.data.contains(item) }
        }
    }

    LaunchedEffect(key1 = isSearchMode) {
        if(!isSearchMode)
        {
            onSearch("")
        }
    }

    LaunchedEffect(key1 = sharedDataRequest) {
        if(sharedDataRequest is DatabaseRequestState.Error)
        {
            Toast.makeText(context, "Request completed with errors.", Toast.LENGTH_SHORT).show()
        }
    }

    CheckNotificationsPermission(
        onPermissionGranted = {
            granted ->
            permissionRequiredDialogVisible = !granted && notificationsAllowed
            if(granted) onNotificationsPreferenceChanged(true)
        }
    )

    NotificationsPermissionRequiredDialog(
        visible = permissionRequiredDialogVisible,
        onPositiveButtonClicked = {
            permissionRequiredDialogVisible = false
            context.openApplicationDetails()
        },
        onNegativeButtonClicked = {
            rememberDecision ->
            if(rememberDecision) onNotificationsPreferenceChanged(false)
            permissionRequiredDialogVisible = false
        }
    )

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
        onNewContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = {
            if(selectedItems.size == 1)
            {
               onContactRenamed(selectedItems.single())
            }
            renameContactDialogVisible = false
        },
        onDismiss = {
            renameContactDialogVisible = false
        }
    )

    SaveNewContactDialog(
        visible = sharedDataRequest is DatabaseRequestState.Success,
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = onContactSaved,
        onDismissClicked = onContactSaveDismissed
    )

    BackHandler(
        enabled = isSearchMode || isSelectionMode
    ) {
        if(isSearchMode)
        {
            isSearchMode = false
        }

        if(isSelectionMode)
        {
            selectedItems.clear()
            isSelectionMode = false
        }
    }

    ExitSelectionMode(
        isSelectionMode = isSelectionMode,
        selectedItemsCount = selectedItems.size,
        onSelectionModeExited = {
            isSelectionMode = false
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
                onSearchTriggered = {
                    isSearchMode = true
                },
                onSearchClicked = {
                    searchQuery -> onSearch(searchQuery)
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
                    if(selectedItems.size == 1)
                    {
                      navigateToAddContactScreen(selectedItems.single().address)
                    }
                },
                onRetryConnection = onRetryConnection
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
        connectionStatus = SignalRStatus.Connected(),
        notificationsAllowed = true,
        onNotificationsPreferenceChanged = {},
        onRetryConnection = {},
        onContactRenamed = { },
        onNewContactNameChanged = { true },
        onDeleteSelectedItems = {},
        onSearch = {},
        contacts = DatabaseRequestState.Success(
            listOf(
                ContactWithConversationPreview(
                    address = "123",
                    name = "John",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                    hasNewMessage = true,
                    lastSynchronized = ZonedDateTime.now()
                ),
                ContactWithConversationPreview(
                    address = "124",
                    name = "Paul",
                    publicKey = "",
                    guardHostname = "",
                    guardAddress = "",
                    hasNewMessage = false,
                    lastSynchronized = ZonedDateTime.now()
                )
            )
        ),
        navigateToChatScreen = {},
        navigateToAddContactScreen = {},
        onContactSaved = {},
        onContactSaveDismissed = {},
        sharedDataRequest = DatabaseRequestState.Idle
    )
}
