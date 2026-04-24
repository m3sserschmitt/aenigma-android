package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.addContacts.AddContactsScreen
import ro.aenigma.services.NavigationTracker
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactsComposable(
    navigationTracker: NavigationTracker,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit
) {
    composable(
        route = Screens.ADD_CONTACTS_PATH,
        arguments = listOf(
            navArgument(Screens.CONTACT_ID_ARG)
            {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
            navArgument(Screens.SCANNER_STATE_ARG) {
                type = NavType.StringType
                nullable = false
                defaultValue = QrCodeScannerState.SCAN_CODE.toString()
            })
    ) { navBackStackEntry ->
        val profileId = navBackStackEntry.arguments?.getString(Screens.CONTACT_ID_ARG)?.takeIf { p -> p.isNotBlank() }
        val scanTypeString =
            navBackStackEntry.arguments?.getString(Screens.SCANNER_STATE_ARG)
                ?: QrCodeScannerState.SCAN_CODE.toString()

        LaunchedEffect(key1 = true) {
            navigationTracker.postCurrentRoute(Screens.ADD_CONTACTS_PATH)
            mainViewModel.init()
        }

        AddContactsScreen(
            profileToShare = profileId,
            initialScannerState = QrCodeScannerState.valueOf(scanTypeString),
            navigateBack = navigateBack,
            mainViewModel = mainViewModel
        )
    }
}
