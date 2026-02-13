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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.VertexDto
import ro.aenigma.ui.screens.common.GenericErrorScreen
import ro.aenigma.ui.screens.common.ItemsList
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.SimpleInfoScreen
import ro.aenigma.ui.screens.common.selectable
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT
import ro.aenigma.util.RequestState
import ro.aenigma.util.StringExtensions.getHost

@Composable
fun ServerItem(
    server: VertexDto,
    onClick: (VertexDto) -> Unit = { }
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
            Icon(
                painter = painterResource(id = R.drawable.ic_storage),
                contentDescription = stringResource(id = R.string.servers),
                tint = MaterialTheme.colorScheme.onBackground
            )
            VerticalDivider(
                color = MaterialTheme.colorScheme.background,
                thickness = 12.dp
            )
            Text(
                text = getHost(
                    hostname = server.neighborhood?.hostname,
                    onionService = server.neighborhood?.onionService
                ),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
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
                    text = stringResource(id = R.string.search),
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
    servers: RequestState<List<VertexDto>>,
    onClick: (VertexDto) -> Unit = { }
) {
    Text(
        modifier = Modifier.padding(bottom = 4.dp),
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground
    )
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
                                onClick = onClick
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
fun ServersBottomSheetContent(
    servers: RequestState<List<VertexDto>>,
    serversHistory: RequestState<List<VertexDto>>,
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = { },
    onSearchClicked: () -> Unit = { },
    onServerClicked: (VertexDto) -> Unit = { },
    onConnectClicked: () -> Unit = { }
) {
    var selectedSection by remember { mutableIntStateOf(0) }

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
            when (selectedSection) {
                0 -> SheetContent(
                    title = stringResource(id = R.string.servers),
                    servers = servers,
                    onClick = onServerClicked,
                )

                1 -> SheetContent(
                    title = stringResource(id = R.string.history),
                    servers = serversHistory,
                    onClick = onServerClicked,
                )
            }
            SearchBar(
                value = searchQuery,
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
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_storage),
                        contentDescription = stringResource(id = R.string.servers),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            )
            NavigationBarItem(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
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
