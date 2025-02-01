package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.contacts.ContactsScreen
import ro.aenigma.services.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.contactsComposable(
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToAddContactScreen: (String?) -> Unit,
    navigateToAboutScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit,
) {
    composable(route = Screens.CONTACTS_SCREEN_ROUTE_FULL) {
        LaunchedEffect(key1 = true)
        {
            mainViewModel.init()
            navigationTracker.postCurrentRoute(Screens.CONTACTS_SCREEN_ROUTE_FULL)
        }

        ContactsScreen(
            navigateToAddContactScreen = navigateToAddContactScreen,
            navigateToChatScreen = navigateToChatScreen,
            navigateToAboutScreen = navigateToAboutScreen,
            mainViewModel = mainViewModel
        )
    }
}
