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

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardAppBar(
    title: String,
    navigateBack: () -> Unit = { },
    navigateBackVisible: Boolean = true,
    transparent: Boolean = false,
    actions: @Composable RowScope.() -> Unit = { },
    navigateBackAlternative: @Composable () -> Unit = { }
) {
    TopAppBar(
        navigationIcon = {
            if (navigateBackVisible) {
                NavigateBackAppBarAction(
                    tint = MaterialTheme.colorScheme.onBackground,
                    onBackClicked = navigateBack
                )
            } else {
                navigateBackAlternative()
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = if (transparent)
                Color.Transparent
            else
                MaterialTheme.colorScheme.background
        ),
        actions = actions
    )
}

@Composable
@Preview
fun StandardAppBarPreview()
{
    StandardAppBar(
        title = "Standard App Bar",
        navigateBack = {},
    )
}
