package com.example.enigma.ui.screens.addContacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.ui.screens.common.EditContactDialog
import com.example.enigma.util.QrCodeScannerState

@Composable
fun SaveNewContactDialog(
    scannerState: QrCodeScannerState,
    onContactNameChanged: (String) -> Boolean,
    onDismissRequest: () -> Unit,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if(scannerState == QrCodeScannerState.SAVE) {
        EditContactDialog(
            onContactNameChanged = onContactNameChanged,
            title = stringResource(
                id = R.string.qr_code_scanned_successfully
            ),
            body = stringResource(
                id = R.string.save_contact_message
            ),
            dismissible = true,
            onDismissRequest = onDismissRequest,
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = onDismissClicked
        )
    }
}

@Preview
@Composable
fun SaveNewContactDialogPreview()
{
    SaveNewContactDialog(
        scannerState = QrCodeScannerState.SAVE,
        onContactNameChanged = { true },
        onConfirmClicked = { },
        onDismissClicked = { },
        onDismissRequest = { }
    )
}
