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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import ro.aenigma.R
import ro.aenigma.ui.navigation.destinations.aboutComposable
import ro.aenigma.ui.navigation.destinations.addContactComposable
import ro.aenigma.ui.navigation.destinations.chatComposable
import ro.aenigma.ui.navigation.destinations.contactsComposable
import ro.aenigma.ui.navigation.destinations.licensesComposable
import ro.aenigma.services.NavigationTracker
import ro.aenigma.ui.navigation.destinations.articleComposable
import ro.aenigma.ui.navigation.destinations.feedComposable
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
    val screen = remember(navHostController) {
        Screens(navController = navHostController)
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(50.dp),
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
    ) { innerPadding ->
        NavHost(
            navHostController,
            startDestination = Screens.STARTING_SCREEN,
            modifier = Modifier.padding(innerPadding)
        ) {
            contactsComposable(
                navigationTracker = navigationTracker,
                navigateToChatScreen = screen.chat,
                navigateToAddContactScreen = screen.addContact,
                navigateToAboutScreen = screen.about,
                mainViewModel = mainViewModel
            )
            chatComposable(
                navigationTracker = navigationTracker,
                navigateToContactsScreen = screen.contacts,
                navigateToAddContactsScreen = screen.addContact
            )
            addContactComposable(
                navigationTracker = navigationTracker,
                navigateToChatsScreen = screen.contacts,
                mainViewModel = mainViewModel
            )
            aboutComposable(
                navigationTracker = navigationTracker,
                navigateToContactsScreen = screen.contacts,
                navigateToLicensesScreen = screen.licenses,
                navigateToPrivacyPolicy = screen.privacyPolicy
            )
            licensesComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateToAboutScreen = screen.about
            )
            feedComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateToArticle = screen.article
            )
            articleComposable(
                navigationTracker = navigationTracker,
                mainViewModel = mainViewModel,
                navigateToFeed = screen.feed
            )
        }
    }
}
