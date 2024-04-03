package com.example.enigma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.enigma.ui.navigation.destinations.addContactComposable
import com.example.enigma.ui.navigation.destinations.chatComposable
import com.example.enigma.ui.navigation.destinations.contactsComposable
import com.example.enigma.util.Constants.Companion.CONTACTS_SCREEN
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun SetupNavigation(
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val screen = remember(navHostController) {
        Screens(navController = navHostController)
    }

    NavHost(
        navController = navHostController,
        startDestination = CONTACTS_SCREEN
    ) {
        contactsComposable(
            navigateToChatScreen = screen.chat,
            navigateToAddContactScreen = screen.addContact,
            mainViewModel = mainViewModel
        )
        chatComposable(
            navigateToContactsScreen = screen.contacts
        )
        addContactComposable(
            navigateToChatsScreen = screen.contacts,
            mainViewModel = mainViewModel
        )
    }
}
