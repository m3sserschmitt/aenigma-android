package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.about.AboutScreen
import ro.aenigma.services.NavigationTracker

fun NavGraphBuilder.aboutComposable (
    navigationTracker: NavigationTracker,
    navigateBack: () -> Unit,
    navigateToLicensesScreen: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit
) {
    composable(
        route = Screens.ABOUT_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            navigationTracker.postCurrentRoute(Screens.ABOUT_SCREEN_PATH)
        }

        AboutScreen(
            navigateBack = navigateBack,
            navigateToLicensesScreen = navigateToLicensesScreen,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy
        )
    }
}
