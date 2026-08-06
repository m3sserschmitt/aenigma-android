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

package ro.aenigma.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import ro.aenigma.R
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.destinations.aboutComposable
import ro.aenigma.ui.navigation.destinations.addContactsComposable
import ro.aenigma.ui.navigation.destinations.addContactsHelpComposable
import ro.aenigma.ui.navigation.destinations.chatComposable
import ro.aenigma.ui.navigation.destinations.contactsComposable
import ro.aenigma.ui.navigation.destinations.licensesComposable
import ro.aenigma.ui.navigation.destinations.articleComposable
import ro.aenigma.ui.navigation.destinations.chatHelpComposable
import ro.aenigma.ui.navigation.destinations.contactsHelpComposable
import ro.aenigma.ui.navigation.destinations.feedComposable
import ro.aenigma.ui.navigation.destinations.feedHelpComposable
import ro.aenigma.ui.navigation.destinations.newPostSheetHelpComposable
import ro.aenigma.ui.navigation.destinations.privacyPolicyComposable
import ro.aenigma.ui.navigation.destinations.serversSheetHelpComposable
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT
import ro.aenigma.util.NavBackStackEntryExtensions.isContactsSelected
import ro.aenigma.util.NavBackStackEntryExtensions.isFeedSelected
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun SetupNavigation(
    notifier: Notifier,
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val isForwardMode by mainViewModel.isForwardMode.collectAsState()
    val screen = remember(navHostController) {
        Screens(navController = navHostController, mainViewModel = mainViewModel)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!isForwardMode) {
                NavigationBar(
                    modifier = Modifier.height(NAVIGATION_BAR_HEIGHT),
                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    NavigationBarItem(
                        selected = backStackEntry.isContactsSelected(),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_people),
                                contentDescription = stringResource(id = R.string.contacts),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            if (!backStackEntry.isContactsSelected()) {
                                screen.contacts()
                            }
                        }
                    )
                    NavigationBarItem(
                        selected = backStackEntry.isFeedSelected(),
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_article),
                                contentDescription = stringResource(id = R.string.news),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        onClick = {
                            if (!backStackEntry.isFeedSelected()) {
                                screen.feed()
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navHostController,
            startDestination = Screens.ROOT_PATH,
            modifier = Modifier.padding(innerPadding)
        ) {
            contactsComposable(
                notifier = notifier,
                navigateToChatScreen = screen.chat,
                navigateToAddContactScreen = screen.addContacts,
                navigateToScanServerScreen = screen.scanServerCode,
                navigateToAboutScreen = screen.about,
                navigateToContactsHelpScreen = screen.contactsHelp,
                navigateToServersSheetHelpScreen = screen.serversSheetHelp,
                navigateToRoot = screen.root,
                mainViewModel = mainViewModel
            )
            chatComposable(
                notifier = notifier,
                navigateBack = screen.back,
                navigateToAddContactsScreen = screen.addContacts,
                redirectUri = screen.forwardUri,
                navigateToArticleScreen = screen.article,
                navigateToChatHelpScreen = screen.chatHelp
            )
            addContactsComposable(
                notifier = notifier,
                navigateBack = screen.back,
                onForwardUri = screen.forwardUri,
                navigateToAddContactsHelpScreen = screen.addContactsHelp,
                navigateToRoot = screen.root,
                mainViewModel = mainViewModel
            )
            aboutComposable(
                notifier = notifier,
                navigateBack = screen.back,
                navigateToLicensesScreen = screen.licenses,
                navigateToPrivacyPolicyScreen = screen.privacyPolicy,
            )
            licensesComposable(
                notifier = notifier,
                navigateBack = screen.back
            )
            feedComposable(
                notifier = notifier,
                mainViewModel = mainViewModel,
                navigateToArticleScreen = screen.article,
                navigateToFeedHelpScreen = screen.feedHelp,
                navigateToNewPostSheetHelpScreen = screen.newPostSheetHelp,
                redirectUri = screen.forwardUri
            )
            articleComposable(
                notifier = notifier,
                mainViewModel = mainViewModel,
                navigateBack = screen.back,
                forwardMessage = screen.forwardMessage
            )
            privacyPolicyComposable(
                notifier = notifier,
                mainViewModel = mainViewModel,
                navigateBack = screen.back
            )
            contactsHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            addContactsHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            feedHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            chatHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            serversSheetHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            newPostSheetHelpComposable(
                notifier = notifier,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
        }
    }
}
