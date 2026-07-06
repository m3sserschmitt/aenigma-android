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

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ContactWithLastMessageDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.ServersSheetStateDto
import ro.aenigma.services.ClientStatus
import ro.aenigma.models.enums.ContactType
import ro.aenigma.models.enums.MessageType
import ro.aenigma.models.enums.ServersSheetSection
import ro.aenigma.models.enums.TorCircuitState
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isFullyExpanded
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isNotFullyExpanded
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toExpanded
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toPartiallyExpanded
import ro.aenigma.models.factories.ContactDtoFactory
import ro.aenigma.models.factories.MessageDtoFactory
import ro.aenigma.ui.screens.common.SnackBar
import ro.aenigma.ui.screens.common.ExitSelectionMode
import ro.aenigma.ui.screens.common.InstallOrbotDialog
import ro.aenigma.ui.screens.common.OrbotInfoDialog
import ro.aenigma.ui.screens.common.RenameContactDialog
import ro.aenigma.ui.screens.common.TorInfoDialog
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.BottomSheetScaffoldStateExtensions.isNotFullyExpanded
import ro.aenigma.util.Constants.Companion.BOTTOM_SHEET_PEEK_HEIGHT
import ro.aenigma.util.Constants.Companion.BROADCAST_CONTACT_ADDRESS
import ro.aenigma.util.ContextExtensions.isOrbotInstalled
import ro.aenigma.util.ContextExtensions.openOrbot
import ro.aenigma.util.ContextExtensions.redirectToOrbotOnPlayStore
import ro.aenigma.util.RequestState
import ro.aenigma.viewmodels.MainViewModel
import java.time.ZonedDateTime

@Composable
fun ContactsScreen(
    navigateToChatScreen: (String) -> Unit,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToScanServerScreen: () -> Unit,
    navigateToAboutScreen: () -> Unit,
    navigateToRoot: () -> Unit,
    mainViewModel: MainViewModel
) {
    val allContacts by mainViewModel.allContacts.collectAsState()
    val servers by mainViewModel.servers.collectAsState()
    val serversHistory by mainViewModel.serversHistory.collectAsState()
    val serversSheetState by mainViewModel.serversSheetState.collectAsState()
    val connectionStatus by mainViewModel.clientStatus.collectAsState()
    val isClientWorkerRunning by mainViewModel.isClientWorkerRunning.collectAsState()
    val useTor by mainViewModel.useTor.collectAsState()
    val useOrbot by mainViewModel.useOrbot.collectAsState()
    val notificationServicePreference by mainViewModel.notificationServicePreference.collectAsState()
    val torCircuitState by mainViewModel.torCircuitState.collectAsState()
    val isForwardMode by mainViewModel.isForwardMode.collectAsState()

    ContactsScreen(
        connectionStatus = connectionStatus,
        isClientWorkerRunning = isClientWorkerRunning,
        contacts = allContacts,
        servers = servers,
        serversHistory = serversHistory,
        serversSheetState = serversSheetState,
        useTor = useTor,
        useOrbot = useOrbot,
        notificationServicePreference = notificationServicePreference,
        torCircuitState = torCircuitState,
        isForwardMode = isForwardMode,
        onTorPreferenceChanged = { useTor -> mainViewModel.torPreferenceChanged(useTor) },
        onOrbotPreferenceChanged = { useOrbot -> mainViewModel.orbotPreferenceChanged(useOrbot) },
        onNotificationServicePreferenceChanged = { notificationServicePreference ->
            mainViewModel.notificationServicePreferenceChanged(notificationServicePreference)
        },
        onRetryConnection = { mainViewModel.syncAndReconnect() },
        onSearch = { searchQuery -> mainViewModel.searchContacts(searchQuery) },
        onServersSearch = { searchQuery -> mainViewModel.searchServers(searchQuery) },
        onServerClicked = { server -> mainViewModel.switchServer(server) },
        onServerConnectClicked = { serverQuery -> mainViewModel.switchServer(serverQuery) },
        onScanServerCodeClicked = navigateToScanServerScreen,
        onConnectPeopleClicked = { mainViewModel.broadcastNewServerLocation() },
        onServersSheetStateChanged = { newSheetState ->
            mainViewModel.setServersSheetState(newSheetState)
        },
        navigateToChatScreen = { chatId ->
            if (isForwardMode) {
                mainViewModel.redirectAttachments(listOf(chatId))
                navigateToRoot()
            } else {
                navigateToChatScreen(chatId)
            }
        },
        onDeleteSelectedItems = { contactsToDelete -> mainViewModel.deleteContacts(contactsToDelete) },
        navigateToAddContactScreen = navigateToAddContactScreen,
        navigateToAboutScreen = navigateToAboutScreen,
        onContactRenamed = { contactToBeRenamed, newName ->
            mainViewModel.renameContact(contactToBeRenamed, newName)
        },
        onNewContactNameChanged = { newValue -> newValue.isNotBlank() },
        onGroupCreated = { selectedItems, name -> mainViewModel.createGroup(selectedItems, name) },
        onResetUserNameClicked = { mainViewModel.resetUserName() },
        onForwardAttachments = { chatIds ->
            mainViewModel.redirectAttachments(chatIds)
            navigateToRoot()
        },
        onRemoveAttachments = {
            mainViewModel.setAttachments(listOf())
            navigateToRoot()
        },
        onContactSaveDismissed = { mainViewModel.resetContactChanges() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    connectionStatus: ClientStatus,
    isClientWorkerRunning: Boolean = false,
    contacts: RequestState<List<ContactWithLastMessageDto>>,
    servers: RequestState<List<ServerInfoDto>>,
    serversHistory: RequestState<List<ServerInfoDto>>,
    serversSheetState: ServersSheetStateDto,
    useTor: Boolean,
    useOrbot: Boolean,
    notificationServicePreference: Boolean = false,
    torCircuitState: TorCircuitState,
    isForwardMode: Boolean = false,
    onTorPreferenceChanged: (Boolean) -> Unit,
    onOrbotPreferenceChanged: (Boolean) -> Unit,
    onNotificationServicePreferenceChanged: (Boolean) -> Unit = { },
    onRetryConnection: () -> Unit,
    onSearch: (String) -> Unit,
    onServersSearch: (String) -> Unit,
    onServerConnectClicked: (String) -> Unit,
    onServerClicked: (ServerInfoDto) -> Unit,
    onScanServerCodeClicked: () -> Unit,
    onConnectPeopleClicked: () -> Unit = { },
    onServersSheetStateChanged: (ServersSheetStateDto) -> Unit,
    onDeleteSelectedItems: (List<ContactWithLastMessageDto>) -> Unit,
    onContactRenamed: (ContactWithLastMessageDto, String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onGroupCreated: (List<ContactWithLastMessageDto>, String) -> Unit,
    onContactSaveDismissed: () -> Unit,
    onResetUserNameClicked: () -> Unit,
    onRemoveAttachments: () -> Unit = { },
    onForwardAttachments: (List<String>) -> Unit = { },
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToAboutScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit
) {
    var createGroupDialogVisible by remember { mutableStateOf(false) }
    var renameContactDialogVisible by remember { mutableStateOf(false) }
    var deleteContactsConfirmationVisible by remember { mutableStateOf(false) }
    var installOrbotDialogVisible by remember { mutableStateOf(false) }
    var orbotInfoDialogVisible by remember { mutableStateOf(false) }
    var torServiceInfoDialogVisible by remember { mutableStateOf(false) }
    var isSearchMode by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var serversSearchQuery by remember { mutableStateOf("") }
    val selectedItems = remember { mutableStateMapOf<String, ContactWithLastMessageDto>() }
    val snackBarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberStandardBottomSheetState(initialValue = serversSheetState.sheetState)
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)
    val cannotShareChannelsString = stringResource(id = R.string.cannot_share_channels)
    val couldNotSelectChannelString = stringResource(id = R.string.cannot_select_channels_to_create_channel)
    val couldNotSelectBroadcastString = stringResource(id = R.string.cannot_select_broadcast_to_create_channel)
    val context = LocalContext.current

    LaunchedEffect(key1 = useOrbot) {
        if(useOrbot) {
            if (!context.isOrbotInstalled()) {
                installOrbotDialogVisible = true
            }
        }
    }

    LaunchedEffect(key1 = serversSheetState.isNotFullyExpanded()) {
        if (serversSheetState.isNotFullyExpanded()) {
             bottomSheetState.partialExpand()
        } else {
            onServersSearch("")
            serversSearchQuery = ""
            bottomSheetState.expand()
        }
    }

    LaunchedEffect(key1 = bottomSheetScaffoldState.isNotFullyExpanded()) {
        if (serversSheetState.isFullyExpanded() && bottomSheetScaffoldState.isNotFullyExpanded()) {
            onServersSheetStateChanged(serversSheetState.toPartiallyExpanded())
        }
    }

    LaunchedEffect(key1 = isSearchMode) {
        if (!isSearchMode) {
            onSearch("")
        }
    }

    InstallOrbotDialog(
        visible = installOrbotDialogVisible,
        onConfirmClicked = {
            installOrbotDialogVisible = false
            onOrbotPreferenceChanged(false)
            context.redirectToOrbotOnPlayStore()
        },
        onDismissClicked = {
            installOrbotDialogVisible = false
            onOrbotPreferenceChanged(false)
        }
    )

    OrbotInfoDialog(
        visible = orbotInfoDialogVisible,
        onLaunchOrbot =  {
            orbotInfoDialogVisible = false
            context.openOrbot()
        },
        onConfirmClicked = {
            orbotInfoDialogVisible = false
            if(!useTor) {
                onOrbotPreferenceChanged(true)
            }
        }
    )

    TorInfoDialog(
        visible = torServiceInfoDialogVisible,
        onLaunchOrbot = {
            torServiceInfoDialogVisible = false
            context.openOrbot()
        },
        onConfirmClicked = {
            torServiceInfoDialogVisible = false
            if(!useOrbot) {
                onTorPreferenceChanged(true)
            }
        }
    )

    DeleteSelectedContactsDialog(
        visible = deleteContactsConfirmationVisible,
        onConfirmClicked = {
            deleteContactsConfirmationVisible = false
            onDeleteSelectedItems(selectedItems.values.toList())
            isSelectionMode = false
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
            val contact = selectedItems.values.singleOrNull()
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

    CreateGroupDialog(
        visible = createGroupDialogVisible,
        onTextChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            onGroupCreated(selectedItems.values.toList(), name)
            createGroupDialogVisible = false
            isSelectionMode = false
            selectedItems.clear()
        },
        onDismissClicked = {
            onContactSaveDismissed()
            createGroupDialogVisible = false
        }
    )

    BackHandler(
        enabled = isSearchMode || isSelectionMode || isForwardMode
    ) {
        if (isSearchMode) {
            isSearchMode = false
        } else if (isSelectionMode) {
            selectedItems.clear()
            isSelectionMode = false
        } else if (isForwardMode) {
            onRemoveAttachments()
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

    SnackBar(
        message = stringResource(id = R.string.connection_failed),
        actionLabel = stringResource(id = R.string.retry),
        visible = connectionStatus is ClientStatus.Error.Aborted,
        snackBarHostState = snackBarHostState,
        onActionPerformed = onRetryConnection
    )

    BottomSheetScaffold(
        containerColor = MaterialTheme.colorScheme.background,
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = if(isForwardMode) { 0.dp } else { BOTTOM_SHEET_PEEK_HEIGHT },
        sheetContent = {
            if(!isForwardMode) {
                ServersBottomSheet(
                    servers = servers,
                    serversHistory = serversHistory,
                    sheetState = serversSheetState,
                    searchQuery = serversSearchQuery,
                    connectionStatus = connectionStatus,
                    isClientWorkerRunning = isClientWorkerRunning,
                    onRetryConnection = onRetryConnection,
                    onSearchQueryChanged = { newSearchQuery ->
                        serversSearchQuery = newSearchQuery
                        if (serversSearchQuery.isEmpty()) {
                            onServersSearch(serversSearchQuery)
                        }
                    },
                    onSearchClicked = { onServersSearch(serversSearchQuery) },
                    onConnectClicked = {
                        if (!serversSearchQuery.isBlank()) {
                            onServerConnectClicked(serversSearchQuery)
                        }
                    },
                    onServerClicked = onServerClicked,
                    onSheetStateChanged = onServersSheetStateChanged,
                    onScanCodeClicked = onScanServerCodeClicked,
                    onConnectPeopleClicked = onConnectPeopleClicked
                )
            }
        },
        sheetContainerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        sheetDragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .75f)
            )
        },
        topBar = {
            ContactsAppBar(
                connectionStatus = connectionStatus,
                isClientWorkerRunning = isClientWorkerRunning,
                isSearchMode = isSearchMode,
                useTor = useTor,
                useOrbot = useOrbot,
                notificationServicePreference = notificationServicePreference,
                torCircuitState = torCircuitState,
                isForwardMode = isForwardMode,
                onTorPreferenceChanged = { activatingTor ->
                    if(activatingTor) {
                        torServiceInfoDialogVisible = true
                    } else {
                        onTorPreferenceChanged(false)
                    }
                },
                onOrbotPreferenceChanged = { activatingOrbot ->
                    if(activatingOrbot) {
                        if(!context.isOrbotInstalled()) {
                            installOrbotDialogVisible = true
                        } else {
                            orbotInfoDialogVisible = true
                        }
                    } else {
                        onOrbotPreferenceChanged(false)
                    }
                },
                onNotificationServicePreferenceChanged = onNotificationServicePreferenceChanged,
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
                onOpenServersList = {
                    if (bottomSheetScaffoldState.isNotFullyExpanded()) {
                        onServersSheetStateChanged(serversSheetState.toExpanded())
                    } else {
                        onServersSheetStateChanged(serversSheetState.toPartiallyExpanded())
                    }
                },
                onDeleteSelectedItemsClicked = {
                    deleteContactsConfirmationVisible = true
                },
                onRenameSelectedItemClicked = {
                    renameContactDialogVisible = true
                },
                onShareSelectedItemsClicked = {
                    val selectedItem = selectedItems.values.singleOrNull()?.contact
                    if (selectedItem != null && selectedItem.type == ContactType.CONTACT) {
                        navigateToAddContactScreen(selectedItem.address)
                    } else if (selectedItem != null && selectedItem.type == ContactType.GROUP) {
                        Toast.makeText(context, cannotShareChannelsString, Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                onCreateGroupClicked = {
                    if (selectedItems.any { item -> item.value.contact.type == ContactType.GROUP }) {
                        Toast.makeText(context, couldNotSelectChannelString, Toast.LENGTH_SHORT)
                            .show()
                    } else if (selectedItems.any { item -> item.value.contact.address == BROADCAST_CONTACT_ADDRESS }) {
                        Toast.makeText(context, couldNotSelectBroadcastString, Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        createGroupDialogVisible = true
                    }
                },
                onResetUsernameClicked = onResetUserNameClicked,
                onForwardAttachments = {
                    onForwardAttachments(selectedItems.keys.toList())
                    selectedItems.clear()
                    isSelectionMode = false
                },
                onRemoveAttachments = onRemoveAttachments,
                onRetryConnection = onRetryConnection,
                navigateToAboutScreen = navigateToAboutScreen
            )
        },
    ) { paddingValues ->
        Scaffold(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding()
            ),
            floatingActionButton = {
                if(!isForwardMode) {
                    ContactsFab(
                        onFabClicked = {
                            navigateToAddContactScreen(null)
                        }
                    )
                }
            }
        ) { paddingValues ->
            ContactsContent(
                modifier = Modifier.padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = BOTTOM_SHEET_PEEK_HEIGHT
                ),
                contacts = contacts,
                isSearchMode = isSearchMode,
                navigateToChatScreen = navigateToChatScreen,
                onItemSelected = { selectedContact ->
                    if (!isSelectionMode) {
                        isSelectionMode = true
                    }

                    selectedItems[selectedContact.contact.address] = selectedContact
                },
                onItemDeselected = { deselectedContact ->
                    selectedItems.remove(deselectedContact.contact.address)
                },
                isSelectionMode = isSelectionMode,
                selectedContacts = selectedItems
            )
        }
    }
}

@Composable
fun ContactsFab(
    onFabClicked: () -> Unit
) {
    FloatingActionButton(
        modifier = Modifier.padding(bottom = BOTTOM_SHEET_PEEK_HEIGHT),
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ContactsScreenPreview() {
    ContactsScreen(
        connectionStatus = ClientStatus.Connected,
        useTor = true,
        useOrbot = false,
        torCircuitState = TorCircuitState.OK,
        onTorPreferenceChanged = { _ -> },
        onOrbotPreferenceChanged = { },
        onRetryConnection = {},
        onContactRenamed = { _, _ -> },
        onNewContactNameChanged = { true },
        onDeleteSelectedItems = {},
        onSearch = {},
        onServersSearch = {},
        onServerClicked = {},
        onServerConnectClicked = {},
        onScanServerCodeClicked = {},
        onServersSheetStateChanged = { },
        onGroupCreated = { _, _ -> },
        contacts = RequestState.Success(
            listOf(
                ContactWithLastMessageDto(
                    ContactDtoFactory.createContact(
                        address = "123",
                        name = "John",
                        publicKey = null,
                        guardHostname = null,
                        guardAddress = null,
                    ), MessageDtoFactory.createOutgoing(
                        chatId = "123",
                        text = "Awesome!",
                        type = MessageType.TEXT,
                        actionFor = null,
                    )
                ),
                ContactWithLastMessageDto(
                    ContactDtoFactory.createContact(
                        address = "124",
                        name = "Elizabeth",
                        publicKey = null,
                        guardHostname = null,
                        guardAddress = null,
                    ), MessageDtoFactory.createIncoming(
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
        servers = RequestState.Idle,
        serversHistory = RequestState.Idle,
        serversSheetState = ServersSheetStateDto(
            sheetState = SheetValue.Hidden,
            selectedSection = ServersSheetSection.SERVERS
        ),
        onResetUserNameClicked = {},
        navigateToChatScreen = {},
        navigateToAddContactScreen = {},
        onContactSaveDismissed = {},
        navigateToAboutScreen = { },
    )
}

@Preview
@Composable
fun ContactsScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        ContactsScreenPreview()
    }
}
