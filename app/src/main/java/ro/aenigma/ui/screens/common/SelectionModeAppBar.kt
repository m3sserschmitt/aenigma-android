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

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

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
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                onCloseClicked = onSelectionModeExited
            )
        },
        title = {
            Text(
                text = when(selectedItemsCount) {
                    0 -> stringResource(id = R.string.no_item_selected)
                    1 -> stringResource(id = R.string.one_item_selected)
                    else -> stringResource(id = R.string.n_items_selected)
                        .format(selectedItemsCount)
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
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
