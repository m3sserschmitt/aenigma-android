package ro.aenigma.ui.screens.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay
import ro.aenigma.services.SignalRStatus
import kotlinx.coroutines.launch

@Composable
fun <T> ConnectionStatusSnackBar(
    connectionStatus: SignalRStatus,
    message: String,
    actionLabel: String,
    targetStatus: Class<T>,
    snackBarHostState: SnackbarHostState,
    onActionPerformed: () -> Unit
) where T : SignalRStatus {
    val coroutineScope = rememberCoroutineScope()
    val currentStatus = rememberUpdatedState(connectionStatus)

    LaunchedEffect(key1 = connectionStatus) {
        delay(15_000L)
        if (targetStatus.isInstance(currentStatus.value) && currentStatus.value == connectionStatus) {
            coroutineScope.launch {
                if (snackBarHostState.currentSnackbarData == null) {
                    val result = snackBarHostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )

                    when (result) {
                        SnackbarResult.ActionPerformed -> onActionPerformed()
                        SnackbarResult.Dismissed -> {}
                    }
                }
            }
        }
    }
}
