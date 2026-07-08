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
import ro.aenigma.ui.screens.addContacts.AddContactsScreen
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactsComposable(
    notifier: Notifier,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit,
    navigateToRoot: () -> Unit,
    onForwardUri: (String) -> Unit,
) {
    composable(
        route = Screens.ADD_CONTACTS_PATH,
        arguments = listOf(
            navArgument(Screens.CONTACT_ID_ARG)
            {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.SCANNER_STATE_ARG) {
                type = NavType.StringType
                nullable = false
                defaultValue = QrCodeScannerState.SCAN_CODE.toString()
            })
    ) { navBackStackEntry ->
        val profileId = navBackStackEntry.arguments?.getString(Screens.CONTACT_ID_ARG)
            ?.takeIf { p -> p.isNotBlank() }
        val uri =
            navBackStackEntry.arguments?.getString(Screens.URI_ARG)?.takeIf { u -> u.isNotBlank() }
        val scanTypeString =
            navBackStackEntry.arguments?.getString(Screens.SCANNER_STATE_ARG)
                ?: QrCodeScannerState.SCAN_CODE.toString()

        LaunchedEffect(key1 = true) {
            mainViewModel.init()
            notifier.enableNotifications()
            notifier.exitChat()
        }

        AddContactsScreen(
            profileToShare = profileId,
            uri = uri,
            initialScannerState = QrCodeScannerState.valueOf(scanTypeString),
            navigateBack = navigateBack,
            onForwardUri = onForwardUri,
            navigateToRoot = navigateToRoot,
            mainViewModel = mainViewModel
        )
    }
}
