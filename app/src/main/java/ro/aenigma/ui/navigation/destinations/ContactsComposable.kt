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
import ro.aenigma.ui.screens.contacts.ContactsScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.contactsComposable(
    mainViewModel: MainViewModel,
    notifier: Notifier,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToScanServerScreen: () -> Unit,
    navigateToAboutScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit,
    navigateToRoot: () -> Unit,
) {
    composable(
        route = Screens.CONTACTS_PATH,
        arguments = listOf(
            navArgument(Screens.URI_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.MESSAGE_ID_ARG) {
                type = NavType.LongType
                defaultValue = -1
            }
        )
    ) { navBackStackEntry ->
        val uri = navBackStackEntry.arguments?.getString(Screens.URI_ARG)?.takeIf { u -> u.isNotBlank() }
        val messageId = navBackStackEntry.arguments?.getLong(Screens.MESSAGE_ID_ARG)?.takeIf { i -> i > 0 }
        LaunchedEffect(key1 = true) {
            mainViewModel.init()
            if (!uri.isNullOrBlank()) {
                mainViewModel.setAttachments(listOf(uri))
            }
            if(messageId != null) {
                mainViewModel.setAttachments(messageId)
            }
            notifier.exitChat()
            notifier.disableNotifications()
        }

        ContactsScreen(
            navigateToAddContactScreen = navigateToAddContactScreen,
            navigateToScanServerScreen = navigateToScanServerScreen,
            navigateToChatScreen = navigateToChatScreen,
            navigateToAboutScreen = navigateToAboutScreen,
            navigateToRoot = navigateToRoot,
            mainViewModel = mainViewModel
        )
    }
}
