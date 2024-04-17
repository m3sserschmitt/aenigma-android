package com.example.enigma.ui.screens.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun ExitSelectionMode(
    isSelectionMode: Boolean,
    selectedItemsCount: Int,
    onSelectionModeExited: () -> Unit
) {
    LaunchedEffect(key1 = isSelectionMode, key2 = selectedItemsCount)
    {
        if(isSelectionMode && selectedItemsCount == 0)
        {
            onSelectionModeExited()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T> Modifier.selectable(
    item: T,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (T) -> Unit,
    onItemDeselected: (T) -> Unit,
    onClick: () -> Unit
): Modifier {
    return this.combinedClickable(
        onClick = {
            if(isSelectionMode && !isSelected){
                onItemSelected(item)
            }
            else if(isSelectionMode)
            {
                onItemDeselected(item)
            } else {
                onClick()
            }
        },
        onLongClick = {
            if(!isSelected)
            {
                onItemSelected(item)
            }
        }
    )
}
