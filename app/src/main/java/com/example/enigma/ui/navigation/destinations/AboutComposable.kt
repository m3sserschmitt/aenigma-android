package com.example.enigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.ui.screens.about.AboutScreen
import com.example.enigma.util.NavigationTracker
import com.example.enigma.viewmodels.MainViewModel

fun NavGraphBuilder.aboutComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToContactsScreen: () -> Unit
) {
    composable(
        route = Screens.ABOUT_SCREEN_ROUTE_FULL
    ) {
        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.ABOUT_SCREEN_ROUTE_FULL)
            mainViewModel.init()
        }

        AboutScreen(
            navigateBack = navigateToContactsScreen
        )
    }
}
