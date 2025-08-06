package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.addContacts.AddContactsScreen
import ro.aenigma.services.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactComposable(
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToChatsScreen: () -> Unit
) {
    composable(
        route = Screens.ADD_CONTACT_SCREEN_ROUTE_FULL,
        arguments = listOf(
            navArgument(Screens.ADD_CONTACTS_SCREEN_CONTACT_ID_ARG)
            {
                type = NavType.StringType
            })
    ) { navBackStackEntry ->
        var profileId =
            navBackStackEntry.arguments!!.getString(Screens.ADD_CONTACTS_SCREEN_CONTACT_ID_ARG)
        if (profileId == null) {
            profileId = Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE
        }

        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.ADD_CONTACT_SCREEN_ROUTE_FULL)
            mainViewModel.init()
        }

        AddContactsScreen(
            profileToShare = profileId,
            navigateToContactsScreen = navigateToChatsScreen,
            mainViewModel = mainViewModel
        )
    }
}
