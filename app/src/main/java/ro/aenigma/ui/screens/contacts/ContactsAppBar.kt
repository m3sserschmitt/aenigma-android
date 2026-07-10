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

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
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
import ro.aenigma.models.enums.TorCircuitState
import ro.aenigma.models.extensions.TorConnectionCheckExtensions.isOk
import ro.aenigma.services.ClientStatus
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.CloseAppBarAction
import ro.aenigma.ui.screens.common.CreateGroupTopAppBarAction
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.DropdownMenuSwitch
import ro.aenigma.ui.screens.common.EditTopAppBarAction
import ro.aenigma.ui.screens.common.ForwardAttachmentsAppBarAction
import ro.aenigma.ui.screens.common.ServersListAppBarAction
import ro.aenigma.ui.screens.common.ReloadClientAppBarAction
import ro.aenigma.ui.screens.common.SearchAppBar
import ro.aenigma.ui.screens.common.SelectionModeAppBar
import ro.aenigma.ui.screens.common.ShareTopAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar

@Composable
fun ContactsAppBar(
    connectionStatus: ClientStatus,
    isClientWorkerRunning: Boolean = false,
    isSelectionMode: Boolean,
    isSearchMode: Boolean,
    selectedItemsCount: Int,
    useTor: Boolean,
    useOrbot: Boolean,
    moreOptionsMenuExpanded: Boolean = false,
    notificationServicePreference: Boolean = false,
    torCircuitState: TorCircuitState,
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
    onNotificationServicePreferenceChanged: (Boolean) -> Unit = { },
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
                ReloadClientAppBarAction(
                    isClientWorkerRunning = isClientWorkerRunning,
                    connectionStatus = connectionStatus,
                    tint = MaterialTheme.colorScheme.onBackground,
                    onClick = onRetryConnection
                )
                ActivateSearchAppBarAction(
                    tint = MaterialTheme.colorScheme.onBackground,
                    onSearchModeTriggered = onSearchTriggered
                )
                if (isSelectionMode && isForwardMode) {
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
                        expanded = moreOptionsMenuExpanded,
                        notificationServicePreference = notificationServicePreference,
                        torCircuitState = torCircuitState,
                        onTorPreferenceChanged = onTorPreferenceChanged,
                        onOrbotPreferenceChanged = onOrbotPreferenceChanged,
                        onNotificationServicePreferenceChanged = onNotificationServicePreferenceChanged
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
    expanded: Boolean = false,
    notificationServicePreference: Boolean,
    torCircuitState: TorCircuitState,
    onTorPreferenceChanged: (Boolean) -> Unit,
    onOrbotPreferenceChanged: (Boolean) -> Unit,
    onNotificationServicePreferenceChanged: (Boolean) -> Unit,
    onResetUsernameClicked: () -> Unit,
    navigateToAboutScreen: () -> Unit
) {
    var isExpanded by remember(key1 = expanded) { mutableStateOf(expanded) }
    BasicDropdownMenu(
        expanded = isExpanded,
        onToggle = { value -> isExpanded = value }
    ) {
        TorSwitch(
            useTor = useTor,
            torCircuitState = torCircuitState,
            onTorPreferenceChanged = onTorPreferenceChanged
        )
        OrbotSwitch(
            useOrbot = useOrbot,
            torCircuitState = torCircuitState,
            onOrbotPreferenceChanged = onOrbotPreferenceChanged
        )
        NotificationServiceSwitch(
            notificationServicePreference = notificationServicePreference,
            onNotificationServicePreferenceChanged = onNotificationServicePreferenceChanged
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(id = R.string.reset_username),
            text = stringResource(id = R.string.reset_username),
            onClick = {
                onResetUsernameClicked()
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.Info,
            contentDescription = stringResource(id = R.string.about_app),
            text = stringResource(id = R.string.about_app),
            onClick = {
                navigateToAboutScreen()
                isExpanded = false
            }
        )
    }
}

@Composable
fun TorSwitch(
    useTor: Boolean,
    torCircuitState: TorCircuitState,
    onTorPreferenceChanged: (Boolean) -> Unit
) {
    DropdownMenuSwitch(
        value = useTor,
        isActive = useTor && torCircuitState.isOk(),
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
    torCircuitState: TorCircuitState,
    onOrbotPreferenceChanged: (Boolean) -> Unit
) {
    DropdownMenuSwitch(
        value = useOrbot,
        isActive = useOrbot && torCircuitState.isOk(),
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

@Composable
fun NotificationServiceSwitch(
    notificationServicePreference: Boolean,
    onNotificationServicePreferenceChanged: (Boolean) -> Unit
) {
    DropdownMenuSwitch(
        value = notificationServicePreference,
        isActive = notificationServicePreference,
        text = stringResource(id = R.string.notification_service),
        icon = {
            Icon(
                modifier = Modifier.alpha(.75f),
                imageVector = Icons.Filled.Notifications,
                contentDescription = stringResource(id = R.string.notification_service),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        onValueChanged = onNotificationServicePreferenceChanged
    )
}

