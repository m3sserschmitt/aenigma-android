package com.example.enigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.screens.addContacts.AddContactsScreen
import com.example.enigma.util.NavigationTracker
import com.example.enigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactComposable(
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToChatsScreen: () -> Unit
) {
    composable(route = Screens.ADD_CONTACT_SCREEN) {
        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.ADD_CONTACT_SCREEN)
        }

        AddContactsScreen(
            navigateToContactsScreen = navigateToChatsScreen,
            mainViewModel = mainViewModel
        )
    }
}
