package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.chat.ChatScreen
import ro.aenigma.services.NavigationTracker
import ro.aenigma.util.ContextExtensions.findActivity
import ro.aenigma.viewmodels.ChatViewModel

fun NavGraphBuilder.chatComposable(
    navigationTracker: NavigationTracker,
    navigateToContactsScreen: () -> Unit,
    navigateToAddContactsScreen: (String) -> Unit
) {
    composable(
        route = Screens.CHAT_SCREEN_ROUTE_FULL,
        arguments = listOf(
            navArgument(Screens.CHAT_SCREEN_CHAT_ID_ARG)
            {
                type = NavType.StringType
            })
    ) { navBackStackEntry ->

        val chatId = navBackStackEntry.arguments!!.getString(Screens.CHAT_SCREEN_CHAT_ID_ARG)
        val chatViewModel: ChatViewModel = hiltViewModel(
            key = chatId,
            viewModelStoreOwner = LocalContext.current.findActivity()
        )

        LaunchedEffect(key1 = true)
        {
            chatViewModel.init()
            if (!chatId.isNullOrBlank()) {
                navigationTracker.postCurrentRoute(Screens.getChatScreenRoute(chatId))
            }
        }

        ChatScreen(
            navigateToContactsScreen = navigateToContactsScreen,
            navigateToAddContactsScreen = navigateToAddContactsScreen,
            chatViewModel = chatViewModel,
            chatId = chatId!!
        )
    }
}
