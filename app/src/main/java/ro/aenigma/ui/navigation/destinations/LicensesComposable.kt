package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.licenses.LicensesScreen

fun NavGraphBuilder.licensesComposable (
    notifier: Notifier,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.LICENSES_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notifier.enableNotifications()
            notifier.exitChat()
        }

        LicensesScreen(
            navigateBack = navigateBack,
        )
    }
}
