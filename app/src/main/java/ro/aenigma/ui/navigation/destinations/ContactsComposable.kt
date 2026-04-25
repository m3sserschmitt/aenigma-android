package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.services.NotificationService
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.contacts.ContactsScreen
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.contactsComposable(
    mainViewModel: MainViewModel,
    notificationService: NotificationService,
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
            notificationService.exitChat()
            notificationService.disableNotifications()
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
