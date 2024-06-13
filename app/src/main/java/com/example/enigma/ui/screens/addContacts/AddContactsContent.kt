package com.example.enigma.ui.screens.addContacts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.ui.screens.common.LoadingScreen
import com.example.enigma.ui.themes.ApplicationComposeTheme
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.QrCodeGenerator
import com.example.enigma.util.QrCodeScannerState

@Composable
fun AddContactsContent(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    qrCodeLabel: String,
    qrCode: DatabaseRequestState<Bitmap>,
    onQrCodeFound: (String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onSaveContact: () -> Unit,
    onSaveContactDismissed: () -> Unit,
) {
    SaveNewContactDialog(
        scannerState = scannerState,
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = onSaveContact,
        onDismissClicked = onSaveContactDismissed,
        onDismissRequest = onSaveContactDismissed
    )

    when(scannerState)
    {
        QrCodeScannerState.SHARE_CODE,
        QrCodeScannerState.SAVE -> {
            DisplayQrCode(
                modifier = modifier,
                qrCode = qrCode,
                qrCodeLabel = qrCodeLabel
            )
        }
        QrCodeScannerState.SCAN_CODE -> {
            QrCodeScanner(
                onQrCodeFound = onQrCodeFound
            )
        }
    }
}

@Composable
fun DisplayQrCode(
    modifier: Modifier = Modifier,
    qrCodeLabel: String,
    qrCode: DatabaseRequestState<Bitmap>,
) {
    when(qrCode) {
        is DatabaseRequestState.Success -> QrCode(
            modifier = modifier,
            qrCodeLabel = qrCodeLabel,
            qrCode = qrCode.data
        )
        is DatabaseRequestState.Error -> CodeNotAvailableError()
        is DatabaseRequestState.Loading -> LoadingScreen()
        is DatabaseRequestState.Idle -> {  }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
@Preview
@Composable
fun AddContactsContentPreview()
{
    val bitmap = QrCodeGenerator(400, 400).encodeAsBitmap("Hello world!")
    if(bitmap != null) {
        ApplicationComposeTheme(darkTheme = true) {
            AddContactsContent(
                scannerState = QrCodeScannerState.SHARE_CODE,
                qrCode = DatabaseRequestState.Success(bitmap),
                qrCodeLabel = "John",
                onQrCodeFound = { },
                onNewContactNameChanged = { true },
                onSaveContact = { },
                onSaveContactDismissed = { }
            )
        }
    }
}
