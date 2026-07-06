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
import ro.aenigma.ui.screens.about.AboutScreen

fun NavGraphBuilder.aboutComposable (
    notifier: Notifier,
    navigateBack: () -> Unit,
    navigateToLicensesScreen: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit
) {
    composable(
        route = Screens.ABOUT_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.enableNotifications()
            notifier.exitChat()
        }

        AboutScreen(
            navigateBack = navigateBack,
            navigateToLicensesScreen = navigateToLicensesScreen,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy
        )
    }
}
