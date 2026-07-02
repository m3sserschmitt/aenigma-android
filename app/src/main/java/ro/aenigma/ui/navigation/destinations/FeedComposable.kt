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

package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.FeedScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.feedComposable (
    notifier: Notifier,
    mainViewModel: MainViewModel,
    navigateToArticle: (uri: String, title: String?, messageId: Long?) -> Unit,
    redirectUri: (String) -> Unit
) {
    composable(
        route = Screens.FEED_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.exitChat()
            notifier.enableNotifications()
        }

        FeedScreen(
            mainViewModel = mainViewModel,
            navigateToArticle = navigateToArticle,
            redirectUri = redirectUri
        )
    }
}
