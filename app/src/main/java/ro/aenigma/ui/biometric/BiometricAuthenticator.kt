package ro.aenigma.ui.biometric

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ro.aenigma.R

@Composable
fun BiometricAuthenticator(
    context: FragmentActivity,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit,
    onAuthFailed: () -> Unit
) {
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val onSuccess = rememberUpdatedState(onAuthSuccess)
    val onError = rememberUpdatedState(onAuthError)
    val onFailed = rememberUpdatedState(onAuthFailed)

    val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    LaunchedEffect(context) {
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess.value()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError.value("$errString")
                }
                override fun onAuthenticationFailed() {
                    onFailed.value()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.access_aenigma))
            .setSubtitle(context.getString(R.string.authenticate_using_biometrics_or_pin))
            .setAllowedAuthenticators(authenticators)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
