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

package ro.aenigma.ui.screens.common

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun CheckScrollPercentage(
    itemsCount: Int,
    state: LazyListState,
    loadNextPage: () -> Unit
) {
    val viewedItems by remember {
        derivedStateOf {
            state.firstVisibleItemIndex + state.layoutInfo.visibleItemsInfo.size
        }
    }

    val scrollPercentage = viewedItems.toFloat() / itemsCount.toFloat() * 100f
    val nextPageRequired = scrollPercentage > 65

    LaunchedEffect(key1 = nextPageRequired)
    {
        if(nextPageRequired)
        {
            loadNextPage()
        }
    }
}

@Composable
fun <K: Any, V> ItemsList(
    modifier: Modifier = Modifier,
    items: List<V>,
    nextPageAvailable: Boolean = false,
    itemKeySelector: (V) -> K,
    selectedItems: Map<K, V> = mapOf(),
    reversedLayout: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    listItem: @Composable (next: V?, entity: V, isSelected: Boolean) -> Unit,
    loadNextPage: () -> Unit = { }
) {
    if (nextPageAvailable) {
        CheckScrollPercentage(
            itemsCount = items.size,
            state = listState,
            loadNextPage = loadNextPage
        )
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        reverseLayout = reversedLayout
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> itemKeySelector(item) }
        ) { index, element ->
            val isSelected = selectedItems.containsKey(itemKeySelector(element))
            val nextItem = if (index < items.size - 1) {
                items[index + 1]
            } else {
                null
            }

            listItem(nextItem, element, isSelected)
        }
    }
}

@Composable
fun <K: Any, V> AutoScrollItemsList(
    modifier: Modifier = Modifier,
    items: List<V>,
    listState: LazyListState = rememberLazyListState(),
    nextPageAvailable: Boolean = false,
    reversedLayout: Boolean = false,
    itemKeySelector: (V) -> K,
    selectedItems: Map<K, V> = mapOf(),
    listItem: @Composable (next: V?, entity: V, isSelected: Boolean) -> Unit,
    loadNextPage: () -> Unit = { },
) {
    ItemsList(
        modifier = modifier,
        items = items,
        nextPageAvailable = nextPageAvailable,
        itemKeySelector = itemKeySelector,
        selectedItems = selectedItems,
        listItem = listItem,
        listState = listState,
        reversedLayout = reversedLayout,
        loadNextPage = loadNextPage
    )

    LaunchedEffect(key1 = items)
    {
        if (items.isNotEmpty() && (listState.firstVisibleItemIndex < 2)) {
            listState.scrollToItem(0)
        }
    }
}
