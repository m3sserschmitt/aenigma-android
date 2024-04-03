package com.example.enigma.ui.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.enigma.ui.screens.contacts.ContactsScreen
import com.example.enigma.util.Constants.Companion.CONTACTS_SCREEN
import com.example.enigma.viewmodels.MainViewModel

fun NavGraphBuilder.contactsComposable(
    navigateToAddContactScreen: () -> Unit,
    navigateToChatScreen: (String) -> Unit,
    mainViewModel: MainViewModel
) {
    composable(route = CONTACTS_SCREEN) {
        ContactsScreen(
            navigateToAddContactScreen = navigateToAddContactScreen,
            navigateToChatScreen = navigateToChatScreen,
            mainViewModel = mainViewModel
        )
    }
}
