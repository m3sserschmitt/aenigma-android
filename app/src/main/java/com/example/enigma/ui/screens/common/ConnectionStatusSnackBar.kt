package com.example.enigma.ui.screens.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.enigma.data.network.SignalRStatus
import kotlinx.coroutines.launch

@Composable
fun <T> ConnectionStatusSnackBar(
    connectionStatus: SignalRStatus,
    message: String,
    actionLabel: String,
    targetStatus: Class<T>,
    snackBarHostState: SnackbarHostState,
    onActionPerformed: () -> Unit
) where T: SignalRStatus {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = connectionStatus)
    {
        if(targetStatus.isInstance(connectionStatus))
        {
            coroutineScope.launch {
                if(snackBarHostState.currentSnackbarData == null) {
                    val result = snackBarHostState.showSnackbar(
                        message = message,
                        actionLabel = actionLabel,
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite
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
