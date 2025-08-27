package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.about.AboutScreen
import ro.aenigma.services.NavigationTracker

fun NavGraphBuilder.aboutComposable (
    navigationTracker: NavigationTracker,
    navigateToContactsScreen: () -> Unit,
    navigateToLicensesScreen: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit
) {
    composable(
        route = Screens.ABOUT_SCREEN_ROUTE_FULL
    ) {
        LaunchedEffect(key1 = true) {
            navigationTracker.postCurrentRoute(Screens.ABOUT_SCREEN_ROUTE_FULL)
        }

        AboutScreen(
            navigateBack = navigateToContactsScreen,
            navigateToLicensesScreen = navigateToLicensesScreen,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy
        )
    }
}
