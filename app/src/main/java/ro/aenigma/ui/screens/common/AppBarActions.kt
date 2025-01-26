package ro.aenigma.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.data.network.SignalRStatus

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
            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
            tint = MaterialTheme.colorScheme.onBackground
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
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ReplyToMessageAppBarAction(
    onReplyToMessageClicked: () -> Unit
) {
    IconButton(
        onClick = onReplyToMessageClicked
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_reply),
            contentDescription = stringResource(
                id = R.string.delete
            ),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
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
            ),
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ConnectionStatusAppBarAction(
    connectionStatus: SignalRStatus
) {
    IndeterminateCircularIndicator(
        size = 18.dp,
        color = MaterialTheme.colorScheme.onBackground,
        textColor = MaterialTheme.colorScheme.onBackground,
        visible = connectionStatus greaterOrEqualThan connectionStatus
                && connectionStatus smallerThan SignalRStatus.Authenticated(),
        text = stringResource(id = R.string.connecting),
        textStyle = MaterialTheme.typography.bodyMedium
    )
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
                ),
                tint = MaterialTheme.colorScheme.onBackground
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
            contentDescription = stringResource(id = R.string.close),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun EditTopAppBarAction(
    visible: Boolean,
    onRenameClicked: () -> Unit
) {
    if(visible) {
        IconButton(
            onClick = onRenameClicked
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(
                    id = R.string.rename
                ),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun ShareTopAppBarAction(
    visible: Boolean,
    onShareContactClick: () -> Unit
) {
    if(visible) {
        IconButton(
            onClick = onShareContactClick
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = stringResource(
                    id = R.string.share
                ),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
