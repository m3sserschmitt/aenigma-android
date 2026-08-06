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
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import ro.aenigma.R
import ro.aenigma.ui.screens.common.CameraPermissionRequiredDialog
import ro.aenigma.ui.screens.common.ErrorScreen
import ro.aenigma.ui.screens.common.RequestPermission
import ro.aenigma.util.ContextExtensions.openApplicationDetails
import ro.aenigma.util.StringExtensions.fromJson
import java.util.concurrent.Executors

@Composable
@OptIn(ExperimentalGetImage::class)
inline fun <reified T> QrCodeScanner(
    modifier: Modifier = Modifier,
    noinline onQrCodeFound: (T) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val currentOnQrCodeFound = rememberUpdatedState(onQrCodeFound)
    var hasMatched by remember { mutableStateOf(false) }
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
        onPermissionGranted = { granted ->
            hasCameraPermission = granted
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
            modifier = modifier,
            factory = { ctx ->
                PreviewView(ctx).apply {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()
                    val scanner = BarcodeScanning.getClient()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder()
                            .build()
                            .also { it.surfaceProvider = this.surfaceProvider }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also { analysis ->
                                analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage == null || hasMatched) {
                                        imageProxy.close()
                                        return@setAnalyzer
                                    }

                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )

                                    scanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            if (hasMatched) return@addOnSuccessListener

                                            for (barcode in barcodes) {
                                                val rawValue = barcode.rawValue ?: continue
                                                val decoded = try {
                                                    rawValue.fromJson<T>()
                                                } catch (_: Exception) {
                                                    null
                                                }

                                                if (decoded != null) {
                                                    hasMatched = true
                                                    cameraProvider.unbindAll()
                                                    currentOnQrCodeFound.value(decoded)
                                                    break
                                                }
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                }
                            }

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                        } catch (_: Exception) {
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )
    } else {
        ErrorScreen(
            text = stringResource(id = R.string.camera_permission_required)
        )
    }
}
