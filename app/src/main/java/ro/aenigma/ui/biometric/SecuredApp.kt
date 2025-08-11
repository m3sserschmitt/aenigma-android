package ro.aenigma.ui.biometric

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.StateFlow
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.util.ContextExtensions.findActivity

@Composable
fun SecuredApp(
    dbPassphraseLoaded: StateFlow<Boolean>,
    isDeviceSecured: Boolean,
    isAuthenticated: Boolean,
    onAuthSuccess: () -> Unit,
    content: (@Composable () -> Unit)
) {
    val context = LocalContext.current.findActivity()
    var authError by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val passphraseOk by dbPassphraseLoaded.collectAsState()

    if (!isAuthenticated && !error && isDeviceSecured) {
        BiometricAuthenticator(
            context = context,
            onAuthSuccess = onAuthSuccess,
            onAuthError = { errorMessage ->
                error = true
                authError = errorMessage
            }
        )
    } else if (passphraseOk && (isAuthenticated || !isDeviceSecured)) {
        content()
    } else if (error) {
        ErrorScreen(
            modifier = Modifier.fillMaxSize(),
            text = authError
        )
    } else {
        LoadingScreen()
    }
}
