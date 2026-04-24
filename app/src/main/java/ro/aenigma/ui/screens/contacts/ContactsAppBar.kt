package ro.aenigma.ui.screens.contacts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.models.enums.TorConnectionCheck
import ro.aenigma.models.extensions.TorConnectionCheckExtensions.isOk
import ro.aenigma.services.SignalRStatus
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.CloseAppBarAction
import ro.aenigma.ui.screens.common.ConnectionStatusAppBarAction
import ro.aenigma.ui.screens.common.CreateGroupTopAppBarAction
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.DropdownMenuSwitch
import ro.aenigma.ui.screens.common.EditTopAppBarAction
import ro.aenigma.ui.screens.common.ForwardAttachmentsAppBarAction
import ro.aenigma.ui.screens.common.ServersListAppBarAction
import ro.aenigma.ui.screens.common.ReloadAppBarAction
import ro.aenigma.ui.screens.common.SearchAppBar
import ro.aenigma.ui.screens.common.SelectionModeAppBar
import ro.aenigma.ui.screens.common.ShareTopAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar

@Composable
fun ContactsAppBar(
    connectionStatus: SignalRStatus,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    selectedItemsCount: Int,
    useTor: Boolean,
    useOrbot: Boolean,
    torConnectionCheck: TorConnectionCheck,
    isForwardMode: Boolean = false,
    onTorPreferenceChanged: (Boolean) -> Unit,
    onOrbotPreferenceChanged: (Boolean) -> Unit,
    onSearchTriggered: () -> Unit,
    onRetryConnection: () -> Unit,
    onSearchModeExited: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSelectionModeExited: () -> Unit,
    onOpenServersList: () -> Unit,
    onDeleteSelectedItemsClicked: () -> Unit,
    onRenameSelectedItemClicked: () -> Unit,
    onShareSelectedItemsClicked: () -> Unit,
    onResetUsernameClicked: () -> Unit,
    onRemoveAttachments: () -> Unit = { },
    onForwardAttachments: () -> Unit = { },
    onCreateGroupClicked: () -> Unit,
    navigateToAboutScreen: () -> Unit
) {
    var searchQueryState by remember { mutableStateOf("") }
    LaunchedEffect(key1 = isSearchMode)
    {
        if (!isSearchMode) {
            searchQueryState = ""
        }
    }

    if (isSearchMode) {
        SearchAppBar(
            searchQuery = searchQueryState,
            onSearchQueryChanged = { newSearchQuery ->
                searchQueryState = newSearchQuery
            },
            onClose = onSearchModeExited,
            onSearchClicked = { searchQuery ->
                onSearchClicked(searchQuery)
            }
        )
    } else if (isSelectionMode && !isForwardMode) {
        SelectionModeAppBar(
            selectedItemsCount = selectedItemsCount,
            onSelectionModeExited = onSelectionModeExited,
            actions = {
                ActivateSearchAppBarAction(
                    onSearchModeTriggered = onSearchTriggered,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                DeleteAppBarAction(
                    onDeleteClicked = onDeleteSelectedItemsClicked,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                EditTopAppBarAction(
                    visible = selectedItemsCount == 1,
                    onRenameClicked = onRenameSelectedItemClicked,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                ShareTopAppBarAction(
                    visible = selectedItemsCount == 1,
                    onClick = onShareSelectedItemsClicked,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                CreateGroupTopAppBarAction(
                    visible = selectedItemsCount > 0,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    onCreateGroupClicked = onCreateGroupClicked
                )
            }
        )
    } else {
        StandardAppBar(
            title = stringResource(
                id = if (!isForwardMode) {
                    R.string.contacts
                } else {
                    R.string.forward
                }
            ),
            navigateBackVisible = false,
            actions = {
                ConnectionStatusAppBarAction(
                    connectionStatus = connectionStatus,
                    tint = MaterialTheme.colorScheme.onBackground
                )
                ReloadAppBarAction(
                    visible = connectionStatus is SignalRStatus.Error.Aborted,
                    tint = MaterialTheme.colorScheme.onBackground,
                    onClick = onRetryConnection
                )
                ActivateSearchAppBarAction(
                    tint = MaterialTheme.colorScheme.onBackground,
                    onSearchModeTriggered = onSearchTriggered
                )
                if(isSelectionMode && isForwardMode) {
                    ForwardAttachmentsAppBarAction(
                        tint = MaterialTheme.colorScheme.onBackground,
                        onForwardAttachments = onForwardAttachments
                    )
                }
                if (!isForwardMode) {
                    MoreActions(
                        navigateToAboutScreen = navigateToAboutScreen,
                        onResetUsernameClicked = onResetUsernameClicked,
                        useTor = useTor,
                        useOrbot = useOrbot,
                        torConnectionCheck = torConnectionCheck,
                        onTorPreferenceChanged = onTorPreferenceChanged,
                        onOrbotPreferenceChanged = onOrbotPreferenceChanged
                    )
                }
            },
            navigateBackAlternative = {
                if (!isForwardMode) {
                    ServersListAppBarAction(
                        tint = MaterialTheme.colorScheme.onBackground,
                        onOpenServersList = onOpenServersList
                    )
                } else {
                    CloseAppBarAction(
                        tint = MaterialTheme.colorScheme.onBackground,
                        onCloseClicked = onRemoveAttachments
                    )
                }
            }
        )
    }
}

@Composable
fun MoreActions(
    useTor: Boolean,
    useOrbot: Boolean,
    torConnectionCheck: TorConnectionCheck,
    onTorPreferenceChanged: (Boolean) -> Unit,
    onOrbotPreferenceChanged: (Boolean) -> Unit,
    onResetUsernameClicked: () -> Unit,
    navigateToAboutScreen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    BasicDropdownMenu(
        expanded = expanded,
        onToggle = { isExpended -> expanded = isExpended }
    ) {
        TorSwitch(
            useTor = useTor,
            torConnectionCheck = torConnectionCheck,
            onTorPreferenceChanged = { activatingTor ->
                if(useOrbot) {
                    onOrbotPreferenceChanged(false)
                }
                onTorPreferenceChanged(activatingTor)
            }
        )
        OrbotSwitch(
            useOrbot = useOrbot,
            torConnectionCheck = torConnectionCheck,
            onOrbotPreferenceChanged = { activatingOrbot ->
                if(useTor) {
                    onTorPreferenceChanged(false)
                }
                onOrbotPreferenceChanged(activatingOrbot)
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(id = R.string.reset_username),
            text = stringResource(id = R.string.reset_username),
            onClick = {
                onResetUsernameClicked()
                expanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Info,
            contentDescription = stringResource(id = R.string.about_app),
            text = stringResource(id = R.string.about_app),
            onClick = {
                navigateToAboutScreen()
                expanded = false
            }
        )
    }
}

@Composable
fun TorSwitch(
    useTor: Boolean,
    torConnectionCheck: TorConnectionCheck,
    onTorPreferenceChanged: (Boolean) -> Unit
) {
    DropdownMenuSwitch(
        value = useTor,
        isActive = useTor && torConnectionCheck.isOk(),
        text = stringResource(id = R.string.tor_service),
        icon = {
            Icon(
                modifier = Modifier.alpha(.75f),
                painter = painterResource(id = R.drawable.ic_vpn),
                contentDescription = stringResource(id = R.string.tor_service),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        onValueChanged = onTorPreferenceChanged
    )
}

@Composable
fun OrbotSwitch(
    useOrbot: Boolean,
    torConnectionCheck: TorConnectionCheck,
    onOrbotPreferenceChanged: (Boolean) -> Unit
) {
    DropdownMenuSwitch(
        value = useOrbot,
        isActive = useOrbot && torConnectionCheck.isOk(),
        text = stringResource(id = R.string.orbot_service),
        icon = {
            Icon(
                modifier = Modifier.alpha(.75f),
                painter = painterResource(id = R.drawable.ic_vpn),
                contentDescription = stringResource(id = R.string.orbot_service),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        onValueChanged = onOrbotPreferenceChanged
    )
}
