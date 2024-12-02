package com.example.enigma.ui.screens.contacts

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enigma.R
import com.example.enigma.data.network.SignalRStatus
import com.example.enigma.ui.screens.common.ActivateSearchAppBarAction
import com.example.enigma.ui.screens.common.DeleteAppBarAction
import com.example.enigma.ui.screens.common.EditTopAppBarAction
import com.example.enigma.ui.screens.common.IndeterminateCircularIndicator
import com.example.enigma.ui.screens.common.RetryConnectionAppBarAction
import com.example.enigma.ui.screens.common.SearchAppBar
import com.example.enigma.ui.screens.common.SelectionModeAppBar
import com.example.enigma.ui.screens.common.ShareTopAppBarAction

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
        DefaultContactsAppBar(
            connectionStatus = connectionStatus,
            onSearchTriggered = onSearchTriggered,
            onRetryConnection = onRetryConnection
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultContactsAppBar(
    connectionStatus: SignalRStatus,
    onRetryConnection: () -> Unit,
    onSearchTriggered: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                maxLines = 1,
                text = stringResource(
                    id = R.string.contacts
                ),
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
        },
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
        }
    )
}

@Composable
@Preview
private fun DefaultContactsAppBarPreview()
{
    DefaultContactsAppBar(
        connectionStatus = SignalRStatus.Authenticated(),
        onRetryConnection = {},
        onSearchTriggered = {}
    )
}

@Composable
@Preview
private fun DefaultConnectingContactsAppBarPreview()
{
    DefaultContactsAppBar(
        connectionStatus = SignalRStatus.Connecting(),
        onRetryConnection = {},
        onSearchTriggered = {}
    )
}
