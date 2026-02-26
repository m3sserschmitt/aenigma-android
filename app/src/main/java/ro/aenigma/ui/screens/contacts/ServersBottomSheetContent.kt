package ro.aenigma.ui.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.models.ServersSheetStateDto
import ro.aenigma.models.enums.ServersSheetSection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isServersHistorySection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.isServersSection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toServersHistorySection
import ro.aenigma.models.extensions.ServersSheetStateDtoExtensions.toServersSection
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.ShareButton
import ro.aenigma.ui.screens.common.SimpleInfoScreen
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT
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
    value: String,
    placeholder: String = stringResource(id = R.string.search),
    onValueChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = value,
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = TextFieldDefaults.colors().copy(
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                errorContainerColor = MaterialTheme.colorScheme.background,
            ),
            onValueChange = onValueChanged,
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .25f),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
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
fun SheetContent(
    title: String,
    servers: RequestState<List<ServerInfoDto>>,
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onScanCodeClicked: () -> Unit = { }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(bottom = 4.dp).weight(1f),
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
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
    when (servers) {
        is RequestState.Success -> {
            if (servers.data.isNotEmpty()) {
                ItemsList(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersBottomSheetContent(
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
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background
            ).border(
                width = .25.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.background
                ).padding(start = 12.dp, top = 12.dp, end = 12.dp)
        ) {
            when (sheetState.selectedSection) {
                ServersSheetSection.SERVERS -> SheetContent(
                    title = stringResource(id = R.string.servers),
                    servers = servers,
                    onServerClicked = onServerClicked,
                    onScanCodeClicked = onScanCodeClicked
                )

                ServersSheetSection.HISTORY -> SheetContent(
                    title = stringResource(id = R.string.history),
                    servers = serversHistory,
                    onServerClicked = onServerClicked,
                    onScanCodeClicked = onScanCodeClicked
                )
            }
            SearchBar(
                value = searchQuery,
                placeholder = stringResource(id = R.string.server_query),
                onValueChanged = { newSearchQuery -> onSearchQueryChanged(newSearchQuery) },
                onSearchClicked = onSearchClicked
            )

            Button(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                onClick = onConnectClicked
            ) {
                Text(
                    text = stringResource(id = R.string.connect),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        NavigationBar(
            modifier = Modifier.fillMaxWidth()
                .height(NAVIGATION_BAR_HEIGHT),
            containerColor = MaterialTheme.colorScheme.background
        ) {
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
    }
}
