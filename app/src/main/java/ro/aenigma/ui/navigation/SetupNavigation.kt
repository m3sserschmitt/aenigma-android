package ro.aenigma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import ro.aenigma.ui.navigation.destinations.aboutComposable
import ro.aenigma.ui.navigation.destinations.addContactComposable
import ro.aenigma.ui.navigation.destinations.chatComposable
import ro.aenigma.ui.navigation.destinations.contactsComposable
import ro.aenigma.ui.navigation.destinations.licensesComposable
import ro.aenigma.util.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun SetupNavigation(
    navigationTracker: NavigationTracker,
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val screen = remember(navHostController) {
        Screens(navController = navHostController)
    }

    NavHost(
        navController = navHostController,
        startDestination = Screens.STARTING_SCREEN
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
            mainViewModel = mainViewModel,
            navigateToContactsScreen = screen.contacts,
            navigateToLicensesScreen = screen.licenses
        )
        licensesComposable(
            navigationTracker = navigationTracker,
            mainViewModel = mainViewModel,
            navigateToAboutScreen = screen.about
        )
    }
}
