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

package ro.aenigma.ui.screens.addContacts

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import ro.aenigma.R
import ro.aenigma.ui.screens.common.CameraPermissionRequiredDialog
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.RequestPermission
import ro.aenigma.util.ContextExtensions.openApplicationDetails
import ro.aenigma.util.QrCodeAnalyzer
import ro.aenigma.util.StringExtensions.fromJson

@Composable
inline fun<reified T> QrCodeScanner(
    crossinline onQrCodeFound: (T) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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
                preview.surfaceProvider = previewView.surfaceProvider
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(ctx),
                    QrCodeAnalyzer { result ->
                        result?.let { decodedData ->
                            val data = decodedData.fromJson<T>()
                            if(data != null) {
                                cameraProviderFuture.get().unbindAll()
                                onQrCodeFound(data)
                            }

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
