package com.example.enigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.screens.contacts.ContactsScreen
import com.example.enigma.util.NavigationTracker
import com.example.enigma.viewmodels.MainViewModel

fun NavGraphBuilder.contactsComposable(
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToAddContactScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit,
) {
    composable(route = Screens.CONTACTS_SCREEN) {
        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.CONTACTS_SCREEN)
        }

        ContactsScreen(
            navigateToAddContactScreen = navigateToAddContactScreen,
            navigateToChatScreen = navigateToChatScreen,
            mainViewModel = mainViewModel
        )
    }
}
