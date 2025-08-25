package ro.aenigma.ui.biometric

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ro.aenigma.R
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.util.ContextExtensions.findActivity

@Composable
fun SecuredApp(
    dbPassphraseLoaded: Boolean,
    isDeviceSecured: Boolean,
    isAuthenticated: Boolean,
    isAuthError: Boolean,
    onAuthSuccess: () -> Unit,
    onAuthFailed: () -> Unit,
    content: (@Composable () -> Unit)
) {
    val context = LocalContext.current.findActivity()
    var authError by remember { mutableStateOf("") }

    if (!isAuthenticated && !isAuthError && isDeviceSecured) {
        BiometricAuthenticator(
            context = context,
            onAuthSuccess = onAuthSuccess,
            onAuthError = { errorMessage ->
                authError = errorMessage
                onAuthFailed()
            },
            onAuthFailed = onAuthFailed
        )
    } else if (dbPassphraseLoaded && (isAuthenticated || !isDeviceSecured)) {
        content()
    } else if (isAuthError) {
        val context = LocalContext.current
        ErrorScreen(
            modifier = Modifier.fillMaxSize(),
            text = if(authError.isBlank()) {
                context.getString(R.string.restart_app_to_retry)
            } else {
                "$authError - ${context.getString(R.string.restart_app_to_retry)}"
            }
        )
    } else {
        LoadingScreen()
    }
}
