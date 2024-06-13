package com.example.enigma.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.enigma.ui.navigation.destinations.addContactComposable
import com.example.enigma.ui.navigation.destinations.chatComposable
import com.example.enigma.ui.navigation.destinations.contactsComposable
import com.example.enigma.util.NavigationTracker
import com.example.enigma.viewmodels.MainViewModel

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
    }
}
