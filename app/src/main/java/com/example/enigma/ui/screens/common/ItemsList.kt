package com.example.enigma.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
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
fun <T> ItemsList(
    modifier: Modifier = Modifier,
    items: List<T>,
    nextPageAvailable: Boolean = false,
    itemKeyProvider: (T) -> Any,
    selectedItems: List<T>,
    reversedLayout: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    listItem: @Composable (entity: T, isSelected: Boolean) -> Unit,
    loadNextPage: () -> Unit = { }
) {
    if(nextPageAvailable) {
        CheckScrollPercentage(
            itemsCount = items.size,
            state = listState,
            loadNextPage = loadNextPage
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = listState,
        reverseLayout = reversedLayout
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> itemKeyProvider(item) }
        ) { _, message ->
            val isSelected = selectedItems.any { item -> item == message }
            listItem(message, isSelected)
        }
    }
}

@Composable
fun <T> AutoScrollItemsList(
    modifier: Modifier = Modifier,
    items: List<T>,
    listState: LazyListState = rememberLazyListState(),
    nextPageAvailable: Boolean = false,
    reversedLayout: Boolean = false,
    itemKeyProvider: (T) -> Any,
    selectedItems: List<T>,
    listItem: @Composable (entity: T, isSelected: Boolean) -> Unit,
    loadNextPage: () -> Unit = { },
) {
    ItemsList(
        modifier = modifier,
        items = items,
        nextPageAvailable = nextPageAvailable,
        itemKeyProvider = itemKeyProvider,
        selectedItems = selectedItems,
        listItem = listItem,
        listState = listState,
        reversedLayout = reversedLayout,
        loadNextPage = loadNextPage
    )

    LaunchedEffect(key1 = items.size)
    {
        if (items.isNotEmpty() && (listState.firstVisibleItemIndex < 2)) {
            listState.scrollToItem(0)
        }
    }
}
