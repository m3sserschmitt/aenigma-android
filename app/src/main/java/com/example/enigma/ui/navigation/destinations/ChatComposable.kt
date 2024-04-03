package com.example.enigma.ui.navigation.destinations

import android.annotation.SuppressLint
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.enigma.ui.screens.chat.ChatScreen
import com.example.enigma.util.Constants
import com.example.enigma.util.Constants.Companion.CHAT_ARGUMENT_KEY
import com.example.enigma.util.findActivity
import com.example.enigma.viewmodels.ChatViewModel

@SuppressLint("StateFlowValueCalledInComposition")
fun NavGraphBuilder.chatComposable(
    navigateToContactsScreen: () -> Unit
) {
    composable(
        route = Constants.CHAT_SCREEN,
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

        ChatScreen(
            navigateToContactsScreen = navigateToContactsScreen,
            chatViewModel = chatViewModel,
            chatId = chatId!!
        )
    }
}
