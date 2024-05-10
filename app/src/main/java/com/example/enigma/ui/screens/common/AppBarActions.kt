package com.example.enigma.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.enigma.R

@Composable
fun CloseAppBarAction(
    onCloseClicked: () -> Unit
) {
    IconButton(
        onClick = onCloseClicked
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = stringResource(
                id = R.string.close
            ),
        )
    }
}

@Composable
fun NavigateBackAppBarAction(
    onBackClicked: () -> Unit
) {
    IconButton(
        onClick = {
            onBackClicked()
        }
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.back),
        )
    }
}

@Composable
fun DeleteAppBarAction(
    onDeleteClicked: () -> Unit
) {
    IconButton(
        onClick = onDeleteClicked
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(
                id = R.string.delete
            ),
        )
    }
}

@Composable
fun ActivateSearchAppBarAction(
    onSearchModeTriggered: () -> Unit
) {
    IconButton(onClick = {
        onSearchModeTriggered()
    }) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource (
                id = R.string.search
            )
        )
    }
}

@Composable
fun RetryConnectionAppBarAction(
    visible: Boolean,
    onRetryConnection: () -> Unit
) {
    if(visible) {
        IconButton(onClick = {
            onRetryConnection()
        }) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(
                    id = R.string.retry_connection
                )
            )
        }
    }
}

@Composable
fun CloseSearchTopAppBarAction(
    isEmptySearchQuery: Boolean,
    onClose: () -> Unit,
    onClearSearchQuery: () -> Unit
) {
    IconButton(
        onClick = {
            when (isEmptySearchQuery) {
                true -> {
                    onClose()
                }
                false -> {
                    onClearSearchQuery()
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(id = R.string.close)
        )
    }
}

@Composable
fun EditTopAppBarAction(
    onRenameClicked: () -> Unit
) {
    IconButton(
        onClick = onRenameClicked
    ) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(
                id = R.string.rename
            ),
        )
    }
}
