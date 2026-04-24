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
import ro.aenigma.ui.navigation.destinations.aboutComposable
import ro.aenigma.ui.navigation.destinations.addContactsComposable
import ro.aenigma.ui.navigation.destinations.chatComposable
import ro.aenigma.ui.navigation.destinations.contactsComposable
import ro.aenigma.ui.navigation.destinations.licensesComposable
import ro.aenigma.services.NavigationTracker
import ro.aenigma.ui.navigation.destinations.articleComposable
import ro.aenigma.ui.navigation.destinations.feedComposable
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT
import ro.aenigma.util.NavBackStackEntryExtensions.isContactsSelected
import ro.aenigma.util.NavBackStackEntryExtensions.isFeedSelected
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun SetupNavigation(
    navigationTracker: NavigationTracker,
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val backStackEntry by navHostController.currentBackStackEntryAsState()
    val isForwardMode by mainViewModel.isForwardMode.collectAsState()
    val screen = remember(navHostController) {
        Screens(navController = navHostController)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if(!isForwardMode) {
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
                            if(!backStackEntry.isContactsSelected()) {
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
                            if(!backStackEntry.isFeedSelected()) {
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
            startDestination = Screens.STARTING_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            contactsComposable(
                navigationTracker = navigationTracker,
                navigateToChatScreen = screen.chat,
                navigateToAddContactScreen = screen.addContacts,
                navigateToScanServerScreen = screen.scanServerCode,
                navigateToAboutScreen = screen.about,
                navigateToRoot = screen.root,
                mainViewModel = mainViewModel
            )
            chatComposable(
                navigationTracker = navigationTracker,
                navigateBack = screen.back,
                navigateToAddContactsScreen = screen.addContacts,
                redirectUri = screen.forwardUri
            )
            addContactsComposable(
                navigationTracker = navigationTracker,
                navigateBack = screen.back,
                mainViewModel = mainViewModel
            )
            aboutComposable(
                navigationTracker = navigationTracker,
                navigateBack = screen.back,
                navigateToLicensesScreen = screen.licenses,
                navigateToPrivacyPolicy = screen.privacyPolicy
            )
            licensesComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateBack = screen.back
            )
            feedComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateToArticle = screen.article,
                redirectUri = screen.forwardUri
            )
            articleComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateBack = screen.back,
                forwardMessage = screen.forwardMessage
            )
        }
    }
}
