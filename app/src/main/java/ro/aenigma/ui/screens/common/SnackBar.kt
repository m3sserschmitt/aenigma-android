package ro.aenigma.ui.screens.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SnackBar(
    visible: Boolean,
    message: String,
    actionLabel: String,
    snackBarHostState: SnackbarHostState,
    onActionPerformed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = visible) {
        if (visible) {
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
