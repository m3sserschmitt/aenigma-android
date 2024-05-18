package com.example.enigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.screens.chat.ChatScreen
import com.example.enigma.util.Constants.Companion.CHAT_ARGUMENT_KEY
import com.example.enigma.util.NavigationTracker
import com.example.enigma.util.findActivity
import com.example.enigma.viewmodels.ChatViewModel

fun NavGraphBuilder.chatComposable(
    navigationTracker: NavigationTracker,
    navigateToContactsScreen: () -> Unit
) {
    composable(
        route = Screens.CHAT_SCREEN,
        arguments = listOf(navArgument(CHAT_ARGUMENT_KEY)
        {
            type = NavType.StringType
        })
    ) {
        navBackStackEntry ->

        val chatId = navBackStackEntry.arguments!!.getString(CHAT_ARGUMENT_KEY)
        val chatViewModel: ChatViewModel = hiltViewModel(
            key = chatId,
            viewModelStoreOwner = LocalContext.current.findActivity()
        )

        LaunchedEffect(key1 = true)
        {
            if (chatId != null) {
                navigationTracker.postCurrentRoute(Screens.getChatScreenRoute(chatId))
            }
        }

        ChatScreen(
            navigateToContactsScreen = navigateToContactsScreen,
            chatViewModel = chatViewModel,
            chatId = chatId!!
        )
    }
}
