package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.licenses.LicensesScreen
import ro.aenigma.util.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.licensesComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateToAboutScreen: () -> Unit
) {
    composable(
        route = Screens.LICENSES_SCREEN_ROUTE_FULL
    ) {
        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.LICENSES_SCREEN_ROUTE_FULL)
            mainViewModel.init()
        }

        LicensesScreen(
            navigateBack = navigateToAboutScreen,
        )
    }
}
