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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT

@Composable
fun BottomSheetTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier.padding(bottom = 4.dp),
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun BottomSheetTemplate(
    navigationBarItems: @Composable (RowScope.() -> Unit) = { },
    content: @Composable (ColumnScope.() -> Unit) = { },
) {
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
            ).padding(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp
            )
    ) {
        content()
        NavigationBar(
            modifier = Modifier.fillMaxWidth()
                .height(NAVIGATION_BAR_HEIGHT),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            navigationBarItems()
        }
    }
}
