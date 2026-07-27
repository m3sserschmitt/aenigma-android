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

package ro.aenigma.ui.screens.feed

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ro.aenigma.ui.screens.common.ShareTopAppBarAction
import ro.aenigma.ui.screens.common.StandardAppBar

@Composable
fun ArticleAppBar(
    uri: String? = null,
    title: String? = null,
    onShareArticle: (String) -> Unit = { },
    navigateBack: () -> Unit = { },
) {
    StandardAppBar(
        title = title.takeIf { t -> !t.isNullOrBlank() } ?: "",
        navigateBack = navigateBack,
        actions = {
            if(!uri.isNullOrBlank()) {
                ShareTopAppBarAction(
                    visible = true,
                    onClick = { onShareArticle(uri) },
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}
