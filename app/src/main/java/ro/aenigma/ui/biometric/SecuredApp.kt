/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

package ro.aenigma.ui.biometric

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
    val context = LocalContext.current
    val restartAppToRetryString = stringResource(id = R.string.restart_app_to_retry)
    val activity = context.findActivity()
    var authError by remember { mutableStateOf("") }

    if (!isAuthenticated && !isAuthError && isDeviceSecured && activity != null) {
        BiometricAuthenticator(
            context = activity,
            onAuthSuccess = onAuthSuccess,
            onAuthError = { errorMessage ->
                authError = errorMessage
                onAuthFailed()
            },
            onAuthFailed = onAuthFailed
        )
    } else if (dbPassphraseLoaded && (isAuthenticated || !isDeviceSecured)) {
        content()
    } else if (isAuthError || activity == null) {
        ErrorScreen(
            modifier = Modifier.fillMaxSize(),
            text = if (authError.isBlank()) {
                restartAppToRetryString
            } else {
                "$authError - $restartAppToRetryString"
            }
        )
    } else {
        LoadingScreen()
    }
}
