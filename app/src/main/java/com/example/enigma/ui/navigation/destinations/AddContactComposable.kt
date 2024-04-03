package com.example.enigma.ui.navigation.destinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.enigma.ui.screens.addContacts.AddContactsScreen
import com.example.enigma.util.Constants
import com.example.enigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactComposable(
    navigateToChatsScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    composable(route = Constants.ADD_CONTACT_SCREEN) {
        AddContactsScreen(
            navigateToContactsScreen = navigateToChatsScreen,
            mainViewModel = mainViewModel
        )
    }
}
