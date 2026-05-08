package ro.aenigma.ui.navigation.destinations

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import ro.aenigma.services.Notifier
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.addContacts.AddContactsScreen
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel

fun NavGraphBuilder.addContactsComposable(
    notifier: Notifier,
    mainViewModel: MainViewModel,
    navigateBack: () -> Unit,
    onForwardUri: (String) -> Unit,
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
        val profileId = navBackStackEntry.arguments?.getString(Screens.CONTACT_ID_ARG)
            ?.takeIf { p -> p.isNotBlank() }
        val uri =
            navBackStackEntry.arguments?.getString(Screens.URI_ARG)?.takeIf { u -> u.isNotBlank() }
        val scanTypeString =
            navBackStackEntry.arguments?.getString(Screens.SCANNER_STATE_ARG)
                ?: QrCodeScannerState.SCAN_CODE.toString()

        LaunchedEffect(key1 = true) {
            mainViewModel.init()
            notifier.enableNotifications()
            notifier.exitChat()
        }

        AddContactsScreen(
            profileToShare = profileId,
            uri = uri,
            initialScannerState = QrCodeScannerState.valueOf(scanTypeString),
            navigateBack = navigateBack,
            onForwardUri = onForwardUri,
            mainViewModel = mainViewModel
        )
    }
}
