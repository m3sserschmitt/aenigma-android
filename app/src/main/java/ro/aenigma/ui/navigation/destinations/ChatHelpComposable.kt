/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2023-2026 Romulus-Emanuel Ruja <romulus.ruja@aenigma.ro>

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
import ro.aenigma.ui.screens.feed.ArticleScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.chatHelpComposable (
    notifier: Notifier,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.CHAT_HELP_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.exitChat()
            notifier.enableNotifications()
            mainViewModel.fetchChatHelp()
        }

        ArticleScreen(
            mainViewModel = mainViewModel,
            navigateBack = navigateBack,
        )
    }
}
