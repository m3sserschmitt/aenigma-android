package ro.aenigma.ui.screens.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.services.SignalRStatus

@Composable
fun CloseAppBarAction(
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun NavigateBackAppBarAction(
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun ServersListAppBarAction(
    tint: Color = Color.Unspecified,
    onOpenServersList: () -> Unit
) {
    IconButton(
        onClick = onOpenServersList
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_storage),
            contentDescription = stringResource(id = R.string.servers),
            tint = tint
        )
    }
}

@Composable
fun ComposeNewArticleAppBarAction(
    tint: Color = Color.Unspecified,
    onComposeNewArticle: () -> Unit,
) {
    IconButton(
        onClick = onComposeNewArticle
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = stringResource(id = R.string.compose_article),
            tint = tint
        )
    }
}

@Composable
fun DeleteAppBarAction(
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun ReplyToMessageAppBarAction(
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun ActivateSearchAppBarAction(
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun ForwardAttachmentsAppBarAction(
    tint: Color = Color.Unspecified,
    onForwardAttachments: () -> Unit
) {
    SendButton(
        tint = tint,
        onClick = onForwardAttachments
    )
}

@Composable
fun ConnectionStatusAppBarAction(
    tint: Color = Color.Unspecified,
    connectionStatus: SignalRStatus
) {
    IndeterminateCircularIndicator(
        size = 18.dp,
        color = tint,
        textColor = tint,
        visible = connectionStatus greaterOrEqualThan SignalRStatus.NotConnected
                && connectionStatus smallerThan SignalRStatus.Authenticated,
        text = stringResource(id = R.string.connecting),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun ReloadAppBarAction(
    visible: Boolean,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    if (visible) {
        IconButton(
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Filled.Refresh,
                contentDescription = stringResource(
                    id = R.string.reload
                ),
                tint = tint
            )
        }
    }
}

@Composable
fun CloseSearchTopAppBarAction(
    isEmptySearchQuery: Boolean,
    tint: Color = Color.Unspecified,
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
            tint = tint
        )
    }
}

@Composable
fun EditTopAppBarAction(
    visible: Boolean,
    tint: Color = Color.Unspecified,
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
                tint = tint
            )
        }
    }
}

@Composable
fun ShareTopAppBarAction(
    visible: Boolean,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    if(visible) {
        ShareButton(
            tint = tint,
            onClick = onClick
        )
    }
}

@Composable
fun CreateGroupTopAppBarAction(
    visible: Boolean,
    tint: Color = Color.Unspecified,
    onCreateGroupClicked: () -> Unit
) {
    if(visible) {
        IconButton(
            onClick = onCreateGroupClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_group),
                contentDescription = stringResource(
                    id = R.string.create_channel
                ),
                tint = tint
            )
        }
    }
}
