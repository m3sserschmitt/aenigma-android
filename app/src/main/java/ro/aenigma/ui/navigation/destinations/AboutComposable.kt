package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.about.AboutScreen

fun NavGraphBuilder.aboutComposable (
    notifier: Notifier,
    navigateBack: () -> Unit,
    navigateToLicensesScreen: () -> Unit,
    navigateToPrivacyPolicy: () -> Unit
) {
    composable(
        route = Screens.ABOUT_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.enableNotifications()
            notifier.exitChat()
        }

        AboutScreen(
            navigateBack = navigateBack,
            navigateToLicensesScreen = navigateToLicensesScreen,
            navigateToPrivacyPolicy = navigateToPrivacyPolicy
        )
    }
}
