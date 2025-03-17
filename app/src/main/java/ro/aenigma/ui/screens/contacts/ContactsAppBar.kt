package ro.aenigma.ui.screens.contacts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.data.network.SignalRStatus
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
        if(!isSearchMode)
        {
            searchQueryState = ""
        }
    }

    if(isSelectionMode)
    {
        SelectionModeAppBar(
            selectedItemsCount = selectedItemsCount,
            onSelectionModeExited = onSelectionModeExited,
            actions = {
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
    } else if (isSearchMode){
        SearchAppBar(
            searchQuery = searchQueryState,
            onSearchQueryChanged = {
                    newSearchQuery -> searchQueryState = newSearchQuery
            },
            onClose = onSearchModeExited,
            onSearchClicked = {
                searchQuery -> onSearchClicked(searchQuery)
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
                    onResetUsernameClicked = onResetUsernameClicked
                )
            }
        )
    }
}

@Composable
fun MoreActions(
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
