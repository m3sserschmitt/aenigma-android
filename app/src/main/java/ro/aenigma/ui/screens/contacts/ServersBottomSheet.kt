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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import ro.aenigma.services.ClientStatus
import ro.aenigma.ui.screens.common.BottomSheetTemplate
import ro.aenigma.ui.screens.common.BottomSheetTitle
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.PrimaryButton
import ro.aenigma.ui.screens.common.ReloadClientAppBarAction
import ro.aenigma.ui.screens.common.ShareTextButton
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
            color = MaterialTheme.colorScheme.primary
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
            val host = getHost(server = server)
            val onionService = getOnionService(server = server)
            Column(
                modifier = Modifier.weight(8f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = host,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if(!onionService.isNullOrBlank()) {
                    Text(
                        text = onionService,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShareTextButton(
                    text = host,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun getHost(server: ServerInfoDto): String {
    var host = server.hostname.getHost()
    if (host.isNullOrBlank()) {
        host = server.onionService.getHost()
    }
    if (host.isNullOrBlank()) {
        host = stringResource(id = R.string.unknown)
    }
    return host
}

@Composable
private fun getOnionService(server: ServerInfoDto): String? {
    return if(server.hostname.isNullOrBlank() && !server.onionService.isNullOrBlank()) {
        null
    } else if(!server.onionService.isNullOrBlank()) {
        server.onionService.getHost()
    } else {
        stringResource(id = R.string.no_onion_service)
    }
}

@Composable
fun SheetEmptySearchResult(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
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
    connectionStatus: ClientStatus = ClientStatus.NotConnected,
    isClientWorkerRunning: Boolean = false,
    onRetryConnection: () -> Unit = { },
    onScanCodeClicked: () -> Unit = { },
    onConnectPeopleClicked: () -> Unit = { }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomSheetTitle(
            modifier = Modifier.weight(1f),
            title = title
        )
        ReloadClientAppBarAction(
            connectionStatus = connectionStatus,
            isClientWorkerRunning = isClientWorkerRunning,
            tint = MaterialTheme.colorScheme.onBackground,
            onClick = onRetryConnection
        )
        IconButton(
            onClick = onConnectPeopleClicked
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_connect_people),
                contentDescription = stringResource(id = R.string.broadcast_new_location),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
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
    connectionStatus: ClientStatus = ClientStatus.NotConnected,
    isClientWorkerRunning: Boolean = false,
    onRetryConnection: () -> Unit = { },
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onScanCodeClicked: () -> Unit = { },
    onConnectPeopleClicked: () -> Unit = { }
) {
    SheetTitleBar(
        title = title,
        connectionStatus = connectionStatus,
        isClientWorkerRunning = isClientWorkerRunning,
        onRetryConnection = onRetryConnection,
        onScanCodeClicked = onScanCodeClicked,
        onConnectPeopleClicked = onConnectPeopleClicked
    )

    when (servers) {
        is RequestState.Success -> {
            if (servers.data.isNotEmpty()) {
                ItemsList(
                    modifier = modifier,
                    items = servers.data,
                    itemKeySelector = { server -> server.address!! },
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
                SheetEmptySearchResult(modifier = modifier)
            }
        }

        is RequestState.Idle,
        is RequestState.Loading -> {
            LoadingScreen(modifier = modifier)
        }

        is RequestState.Error -> {
            GenericErrorScreen(modifier = modifier)
        }
    }
}

@Composable
fun SheetContent(
    modifier: Modifier = Modifier,
    sheetState: ServersSheetStateDto,
    servers: RequestState<List<ServerInfoDto>>,
    serversHistory: RequestState<List<ServerInfoDto>>,
    connectionStatus: ClientStatus = ClientStatus.NotConnected,
    isClientWorkerRunning: Boolean = false,
    onRetryConnection: () -> Unit = { },
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onScanCodeClicked: () -> Unit = { },
    onConnectPeopleClicked: () -> Unit = { }
) {
    when (sheetState.selectedSection) {
        ServersSheetSection.SERVERS -> SheetContent(
            modifier = modifier,
            title = stringResource(id = R.string.servers),
            servers = servers,
            connectionStatus = connectionStatus,
            isClientWorkerRunning = isClientWorkerRunning,
            onRetryConnection = onRetryConnection,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked,
            onConnectPeopleClicked = onConnectPeopleClicked,
        )

        ServersSheetSection.HISTORY -> SheetContent(
            modifier = modifier,
            title = stringResource(id = R.string.history),
            servers = serversHistory,
            connectionStatus = connectionStatus,
            isClientWorkerRunning = isClientWorkerRunning,
            onRetryConnection = onRetryConnection,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked,
            onConnectPeopleClicked = onConnectPeopleClicked
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
    connectionStatus: ClientStatus = ClientStatus.NotConnected,
    isClientWorkerRunning: Boolean = false,
    onRetryConnection: () -> Unit = { },
    onSheetStateChanged: (ServersSheetStateDto) -> Unit = { },
    onSearchQueryChanged: (String) -> Unit = { },
    onSearchClicked: () -> Unit = { },
    onServerClicked: (ServerInfoDto) -> Unit = { },
    onConnectClicked: () -> Unit = { },
    onScanCodeClicked: () -> Unit = { },
    onConnectPeopleClicked: () -> Unit = { }
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
                .weight(1f),
            sheetState = sheetState,
            serversHistory = serversHistory,
            servers = servers,
            connectionStatus = connectionStatus,
            isClientWorkerRunning = isClientWorkerRunning,
            onRetryConnection = onRetryConnection,
            onServerClicked = onServerClicked,
            onScanCodeClicked = onScanCodeClicked,
            onConnectPeopleClicked = onConnectPeopleClicked
        )
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            placeholder = stringResource(id = R.string.server_query),
            onValueChanged = { newSearchQuery -> onSearchQueryChanged(newSearchQuery) },
            onSearchClicked = onSearchClicked
        )
        PrimaryButton(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 4.dp),
            text = stringResource(id = R.string.connect),
            onClick = onConnectClicked
        )
    }
}
