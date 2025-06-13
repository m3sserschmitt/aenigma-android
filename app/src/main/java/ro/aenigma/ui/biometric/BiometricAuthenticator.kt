package ro.aenigma.ui.biometric

import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import ro.aenigma.R

@Composable
fun BiometricAuthenticator(
    context: FragmentActivity,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    LaunchedEffect(key1 = true) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            context,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onAuthError("$errString")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onAuthError(context.getString(R.string.authentication_failed))
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.access_database))
            .setSubtitle(context.getString(R.string.authenticate_using_biometrics_or_pin))
            .setDeviceCredentialAllowed(true)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
