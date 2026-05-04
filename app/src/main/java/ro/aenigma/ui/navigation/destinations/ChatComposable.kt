package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.chat.ChatScreen
import ro.aenigma.util.ContextExtensions.findActivity
import ro.aenigma.viewmodels.ChatViewModel

fun NavGraphBuilder.chatComposable(
    notifier: Notifier,
    redirectUri: (String) -> Unit,
    navigateBack: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    composable(
        route = Screens.CHAT_PATH,
        arguments = listOf(
            navArgument(Screens.CHAT_ID_ARG) { type = NavType.StringType }
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = Screens.CHAT_DEEP_LINK }
        )
    ) { navBackStackEntry ->

        val chatId = navBackStackEntry.arguments?.getString(Screens.CHAT_ID_ARG)
            ?.takeIf { id -> id.isNotBlank() }
        val context = LocalContext.current
        val activity = context.findActivity()
        val chatViewModel: ChatViewModel = if (activity == null) {
            hiltViewModel(key = chatId)
        } else {
            hiltViewModel(
                key = chatId,
                viewModelStoreOwner = activity
            )
        }

        LaunchedEffect(key1 = true) {
            chatViewModel.init()
            notifier.enableNotifications()
            notifier.enterChat(chatId)
        }

        ChatScreen(
            navigateBack = navigateBack,
            navigateToAddContactsScreen = navigateToAddContactsScreen,
            redirectUri = redirectUri,
            chatViewModel = chatViewModel,
            chatId = chatId
        )
    }
}
