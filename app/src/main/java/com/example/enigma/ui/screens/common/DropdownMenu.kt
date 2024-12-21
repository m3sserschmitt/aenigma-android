package com.example.enigma.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.enigma.R

@Composable
fun BasicDropdownMenu(
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    Box {
        IconButton(
            onClick = {
                onToggle(!expanded)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(
                    id = R.string.more_actions
                )
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                onToggle(false)
            },
            content = actions
        )
    }
}
