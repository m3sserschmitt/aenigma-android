package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ro.aenigma.R
import ro.aenigma.data.network.SignalRStatus
import ro.aenigma.ui.screens.common.ActivateSearchAppBarAction
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.DeleteAppBarAction
import ro.aenigma.ui.screens.common.EditTopAppBarAction
import ro.aenigma.ui.screens.common.IndeterminateCircularIndicator
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
                IndeterminateCircularIndicator(
                    modifier = Modifier.size(18.dp),
                    visible = connectionStatus greaterOrEqualThan connectionStatus
                            && connectionStatus smallerThan SignalRStatus.Authenticated(),
                    text = stringResource(id = R.string.connecting),
                    fontSize = 12.sp
                )
                RetryConnectionAppBarAction(
                    visible = connectionStatus is SignalRStatus.Error.Aborted,
                    onRetryConnection = onRetryConnection
                )
                ActivateSearchAppBarAction(
                    onSearchModeTriggered = onSearchTriggered
                )
                MoreActions(
                    navigateToAboutScreen = navigateToAboutScreen
                )
            }
        )
    }
}

@Composable
fun MoreActions(
    navigateToAboutScreen: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    BasicDropdownMenu(
        expanded = expanded,
        onToggle = { isExpended ->
            expanded = isExpended
        }
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = stringResource(
                        id = R.string.about_app
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.about_app
                    )
                )
            },
            onClick = {
                navigateToAboutScreen()
                expanded = false
            }
        )
    }
}
