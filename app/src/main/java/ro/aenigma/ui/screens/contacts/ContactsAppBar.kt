package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.services.SignalRStatus
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.ConnectionStatusAppBarAction
import ro.aenigma.ui.screens.common.CreateGroupTopAppBarAction
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.EditTopAppBarAction
import ro.aenigma.ui.screens.common.RetryConnectionAppBarAction
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
    useTorChanged: (Boolean) -> Unit,
    onSearchTriggered: () -> Unit,
    onRetryConnection: () -> Unit,
    onSearchModeExited: () -> Unit,
    onSearchClicked: (String) -> Unit,
    onSelectionModeExited: () -> Unit,
    onDeleteSelectedItemsClicked: () -> Unit,
    onRenameSelectedItemClicked: () -> Unit,
    onShareSelectedItemsClicked: () -> Unit,
    onResetUsernameClicked: () -> Unit,
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
    } else if (isSelectionMode) {
        SelectionModeAppBar(
            selectedItemsCount = selectedItemsCount,
            onSelectionModeExited = onSelectionModeExited,
            actions = {
                ActivateSearchAppBarAction(
                    onSearchModeTriggered = onSearchTriggered,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                DeleteAppBarAction(
                    onDeleteClicked = onDeleteSelectedItemsClicked
                )
                EditTopAppBarAction(
                    visible = selectedItemsCount == 1,
                    onRenameClicked = onRenameSelectedItemClicked
                )
                ShareTopAppBarAction(
                    visible = selectedItemsCount == 1,
                    onShareContactClick = onShareSelectedItemsClicked
                )
                CreateGroupTopAppBarAction(
                    visible = selectedItemsCount > 0,
                    onCreateGroupClicked = onCreateGroupClicked
                )
            }
        )
    } else {
        StandardAppBar(
            title = stringResource(
                id = R.string.contacts
            ),
            navigateBackVisible = false,
            actions = {
                ConnectionStatusAppBarAction(
                    connectionStatus = connectionStatus
                )
                RetryConnectionAppBarAction(
                    visible = connectionStatus is SignalRStatus.Error.Aborted,
                    onRetryConnection = onRetryConnection
                )
                ActivateSearchAppBarAction(
                    onSearchModeTriggered = onSearchTriggered
                )
                MoreActions(
                    navigateToAboutScreen = navigateToAboutScreen,
                    onResetUsernameClicked = onResetUsernameClicked,
                    useTor = useTor,
                    useTorChanged = useTorChanged
                )
            }
        )
    }
}

@Composable
fun MoreActions(
    useTor: Boolean,
    useTorChanged: (Boolean) -> Unit,
    onResetUsernameClicked: () -> Unit,
    navigateToAboutScreen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    BasicDropdownMenu(
        expanded = expanded,
        onToggle = { isExpended ->
            expanded = isExpended
        }
    ) {
        TorSwitch(
            useTor = useTor,
            useTorChanged = useTorChanged
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
    useTorChanged: (Boolean) -> Unit
) {
    DropdownMenuItem(
        enabled = true,
        leadingIcon = {
            Icon(
                modifier = Modifier.alpha(.75f),
                painter = painterResource(id = R.drawable.ic_vpn),
                contentDescription = stringResource(id = R.string.tor),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = useTor,
                    onCheckedChange = useTorChanged,
                    colors = SwitchDefaults.colors().copy(
                        checkedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                Text(
                    text = stringResource(id = R.string.tor),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        onClick = {
            useTorChanged(!useTor)
        }
    )
}
