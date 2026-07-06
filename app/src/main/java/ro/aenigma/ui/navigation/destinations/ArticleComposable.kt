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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.feed.ArticleScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.articleComposable (
    notifier: Notifier,
    mainViewModel: MainViewModel,
    forwardMessage: (Long) -> Unit,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.ARTICLE_SCREEN_PATH,
        arguments = listOf(
            navArgument(Screens.URI_ARG)
            {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.TITLE_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.MESSAGE_ID_ARG) {
                type = NavType.LongType
                defaultValue = Long.MIN_VALUE
            }
        )
    ) { navBackStackEntry ->
        val uri = navBackStackEntry.arguments?.getString(Screens.URI_ARG)?.takeIf { t -> t.isNotBlank() }
        val title = navBackStackEntry.arguments?.getString(Screens.TITLE_ARG)?.takeIf { t -> t.isNotBlank() }
        val messageId = navBackStackEntry.arguments?.getLong(Screens.MESSAGE_ID_ARG)?.takeIf { id -> id > 0 }

        LaunchedEffect(key1 = true) {
            notifier.exitChat()
            notifier.enableNotifications()
        }

        ArticleScreen(
            uri = uri,
            title = title,
            messageId = messageId,
            mainViewModel = mainViewModel,
            forwardMessage = forwardMessage,
            navigateBack = navigateBack,
        )
    }
}
