package ro.aenigma.ui.screens.common

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun RequestPermission(
    permission: String,
    onPermissionGranted: (Boolean) -> Unit
) {
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
                granted -> onPermissionGranted(granted)
        }
    )

    LaunchedEffect(key1 = true) {
        requestPermissionLauncher.launch(permission)
    }
}

@Composable
fun CheckNotificationsPermission(
    onPermissionGranted: (Boolean) -> Unit
) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        RequestPermission(
            Manifest.permission.POST_NOTIFICATIONS,
            onPermissionGranted = onPermissionGranted
        )
    }
}
