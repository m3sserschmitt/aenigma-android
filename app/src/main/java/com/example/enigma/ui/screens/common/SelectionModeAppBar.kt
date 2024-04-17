package com.example.enigma.ui.screens.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeAppBar(
    selectedItemsCount: Int,
    onSelectionModeExited: () -> Unit,
    actions: @Composable () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            CloseAppBarAction(
                onCloseClicked = onSelectionModeExited
            )
        },
        title = {
            Text(text = "$selectedItemsCount items selected")
        },
        actions = {
            actions()
        }
    )
}

@Preview
@Composable
fun SelectionModeAppBarPreview()
{
    SelectionModeAppBar(
        selectedItemsCount = 3,
        onSelectionModeExited = { },
        actions = {}
    )
}
