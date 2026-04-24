package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.licenses.LicensesScreen
import ro.aenigma.services.NavigationTracker
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.licensesComposable (
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.LICENSES_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true)
        {
            navigationTracker.postCurrentRoute(Screens.LICENSES_SCREEN_PATH)
            mainViewModel.init()
        }

        LicensesScreen(
            navigateBack = navigateBack,
        )
    }
}
