package com.example.enigma.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun <T> ItemsList(
    modifier: Modifier = Modifier,
    items: List<T>,
    itemKeyProvider: (T) -> Any,
    selectedItems: List<T>,
    itemEqualityChecker: (T, T) -> Boolean,
    state: LazyListState = rememberLazyListState(),
    listItem: @Composable (entity: T, isSelected: Boolean) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        state = state
    ) {
        this.items(
            items = items,
            key = itemKeyProvider
        ) { message ->
            val isSelected = selectedItems.any { item -> itemEqualityChecker(item, message) }
            listItem(message, isSelected)
        }
    }
}

@Composable
fun <T> AutoScrollItemsList(
    modifier: Modifier = Modifier,
    items: List<T>,
    itemKeyProvider: (T) -> Any,
    selectedItems: List<T>,
    itemEqualityChecker: (T, T) -> Boolean,
    listItem: @Composable (entity: T, isSelected: Boolean) -> Unit
) {
    val columnState = rememberLazyListState()

    ItemsList(
        modifier = modifier,
        items = items,
        itemKeyProvider = itemKeyProvider,
        selectedItems = selectedItems,
        itemEqualityChecker = itemEqualityChecker,
        listItem = listItem
    )

    LaunchedEffect(key1 = items.size)
    {
        if (items.isNotEmpty()) {
            columnState.scrollToItem(items.size - 1)
        }
    }
}
