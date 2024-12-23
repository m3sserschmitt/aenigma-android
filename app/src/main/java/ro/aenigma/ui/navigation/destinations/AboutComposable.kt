package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.about.AboutScreen
import ro.aenigma.util.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

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
