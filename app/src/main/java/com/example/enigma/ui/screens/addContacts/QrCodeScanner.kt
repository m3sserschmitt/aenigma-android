package com.example.enigma.ui.screens.addContacts

import android.Manifest
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.enigma.R
import com.example.enigma.ui.screens.common.CameraPermissionRequiredDialog
import com.example.enigma.ui.screens.common.ErrorScreen
import com.example.enigma.ui.screens.common.RequestPermission
import com.example.enigma.util.QrCodeAnalyzer
import com.example.enigma.util.openApplicationDetails

@Composable
fun QrCodeScanner(
    onQrCodeFound: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraPermissionDialogVisible by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    RequestPermission(
        permission = Manifest.permission.CAMERA,
        onPermissionGranted = {
            granted -> hasCameraPermission = granted
            cameraPermissionDialogVisible = !granted
        }
    )

    CameraPermissionRequiredDialog(
        visible = cameraPermissionDialogVisible,
        onPositiveButtonClicked = {
            cameraPermissionDialogVisible = false
            context.openApplicationDetails()
        },
        onNegativeButtonClicked = {
            cameraPermissionDialogVisible = false
        }
    )

    if (hasCameraPermission) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val preview = Preview.Builder().build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(ctx),
                    QrCodeAnalyzer { result ->
                        result?.let { decodedData ->
                            cameraProviderFuture.get().unbindAll()
                            onQrCodeFound(decodedData)
                        }
                    }
                )

                try {
                    cameraProviderFuture.get().unbindAll()
                    cameraProviderFuture.get().bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return@AndroidView previewView
            }
        )
    }
    else
    {
        ErrorScreen(
            text = stringResource(id = R.string.camera_permission_required)
        )
    }
}
