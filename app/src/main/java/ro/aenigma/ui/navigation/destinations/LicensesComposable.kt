package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import ro.aenigma.services.NotificationService
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.licenses.LicensesScreen

fun NavGraphBuilder.licensesComposable (
    notificationService: NotificationService,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.LICENSES_SCREEN_PATH
    ) {
        LaunchedEffect(key1 = true) {
            notificationService.enableNotifications()
            notificationService.exitChat()
        }

        LicensesScreen(
            navigateBack = navigateBack,
        )
    }
}
