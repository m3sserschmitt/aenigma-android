package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.ServersSheetStateDto
import ro.aenigma.models.enums.ServersSheetSection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isServersHistorySection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isServersSection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toServersHistorySection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toServersSection
import ro.aenigma.ui.screens.common.BottomSheetTemplate
import ro.aenigma.ui.screens.common.BottomSheetTitle
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.PrimaryButton
import ro.aenigma.ui.screens.common.ShareButton
import ro.aenigma.ui.screens.common.SimpleInfoScreen
import ro.aenigma.ui.screens.common.SimpleTextInput
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.util.RequestState
import ro.aenigma.util.StringExtensions.getHost

@Composable
fun ServerItem(
    server: ServerInfoDto,
    onClick: (ServerInfoDto) -> Unit = { }
) {
    Card(
        modifier = Modifier.selectable(
            item = server,
            onClick = { item -> onClick(item) },
            isSelectionMode = false,
            isSelected = false,
            onItemSelected = { },
            onItemDeselected = { }
        ).border(
            width = 4.dp,
            color = MaterialTheme.colorScheme.primaryContainer
        ).padding(12.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_storage),
                    contentDescription = stringResource(id = R.string.servers),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            val host = getHost(
                hostname = server.hostname,
                onionService = server.onionService
            )
            Box(
                modifier = Modifier.weight(8f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = host,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShareButton(
                    text = host,
                    iconTint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun getHost(hostname: String?, onionService: String?): String {
    var host = hostname?.getHost()
    if(host.isNullOrBlank()) {
        host = onionService?.getHost()
    }
    if(host.isNullOrBlank())
    {
        host = stringResource(id = R.string.unknown)
    }
    return host
}

@Composable
fun SheetPlaceholderContainer(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        content()
    }
}

@Composable
fun SheetLoadingScreen() {
    SheetPlaceholderContainer {
        LoadingScreen()
    }
}

@Composable
fun SheetErrorScreen() {
    SheetPlaceholderContainer {
        GenericErrorScreen()
    }
}

@Composable
fun SheetEmptySearchResult() {
    SheetPlaceholderContainer {
        SimpleInfoScreen(
            message = stringResource(
                id = R.string.no_server_found
            ),
            icon = painterResource(
                id = R.drawable.ic_storage
            ),
            contentDescription = stringResource(
                id = R.string.no_server_found
            )
        )
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String = stringResource(id = R.string.search),
    onValueChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SimpleTextInput(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChanged = onValueChanged,
            placeholder = placeholder,
            singleLine = true
        )
        IconButton(
            onClick = onSearchClicked
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = stringResource(id = R.string.search),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SheetTitleBar(
    title: String,
    onScanCodeClicked: () -> Unit = { }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomSheetTitle(
            modifier = Modifier.weight(1f),
            title = title
        )
        IconButton(
            onClick = onScanCodeClicked
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_qr_code),
                contentDescription = stringResource(id = R.string.scan_qr_code),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SheetContent(
    modifier: Modifier = Modifier,
    title: String,
    servers: RequestState<List<ServerInfoDto>>,
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onScanCodeClicked: () -> Unit = { }
) {
    SheetTitleBar(
        title = title,
        onScanCodeClicked = onScanCodeClicked
    )

    when (servers) {
        is RequestState.Success -> {
            if (servers.data.isNotEmpty()) {
                ItemsList(
                    modifier = modifier,
                    items = servers.data,
                    itemKeyProvider = { server -> server.address!! },
                    listItem = { _, server, _ ->
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                        ) {
                            ServerItem(
                                server = server,
                                onClick = onServerClicked
                            )
                        }
                    },
                )
            } else {
                SheetEmptySearchResult()
            }
        }

        is RequestState.Idle,
        is RequestState.Loading -> {
            SheetLoadingScreen()
        }

        is RequestState.Error -> {
            SheetErrorScreen()
        }
    }
}

@Composable
fun SheetContent(
    modifier: Modifier = Modifier,
    sheetState: ServersSheetStateDto,
    servers: RequestState<List<ServerInfoDto>>,
    serversHistory: RequestState<List<ServerInfoDto>>,
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onScanCodeClicked: () -> Unit = { }
) {
    when (sheetState.selectedSection) {
        ServersSheetSection.SERVERS -> SheetContent(
            modifier = modifier,
            title = stringResource(id = R.string.servers),
            servers = servers,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked
        )

        ServersSheetSection.HISTORY -> SheetContent(
            modifier = modifier,
            title = stringResource(id = R.string.history),
            servers = serversHistory,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersBottomSheet(
    servers: RequestState<List<ServerInfoDto>>,
    serversHistory: RequestState<List<ServerInfoDto>>,
    searchQuery: String = "",
    sheetState: ServersSheetStateDto,
    onSheetStateChanged: (ServersSheetStateDto) -> Unit = { },
    onSearchQueryChanged: (String) -> Unit = { },
    onSearchClicked: () -> Unit = { },
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onConnectClicked: () -> Unit = { },
    onScanCodeClicked: () -> Unit = { }
) {
    BottomSheetTemplate(
        navigationBarItems = {
            NavigationBarItem(
                selected = sheetState.isServersSection(),
                onClick = { onSheetStateChanged(sheetState.toServersSection()) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_storage),
                        contentDescription = stringResource(id = R.string.servers),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
            NavigationBarItem(
                selected = sheetState.isServersHistorySection(),
                onClick = { onSheetStateChanged(sheetState.toServersHistorySection()) },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history),
                        contentDescription = stringResource(id = R.string.history),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
        }
    ) {
        SheetContent(
            modifier = Modifier.fillMaxWidth()
                .weight(7.5f),
            sheetState = sheetState,
            serversHistory = serversHistory,
            servers = servers,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked
        )
        SearchBar(
            modifier = Modifier.fillMaxWidth()
                .weight(.75f),
            value = searchQuery,
            placeholder = stringResource(id = R.string.server_query),
            onValueChanged = { newSearchQuery -> onSearchQueryChanged(newSearchQuery) },
            onSearchClicked = onSearchClicked
        )
        PrimaryButton(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 4.dp)
                .weight(.75f),
            text = stringResource(id = R.string.connect),
            onClick = onConnectClicked
        )
    }
}
