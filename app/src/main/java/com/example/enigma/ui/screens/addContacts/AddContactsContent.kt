package com.example.enigma.ui.screens.addContacts

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.enigma.R
import com.example.enigma.ui.screens.common.EditContactDialog
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.QrCodeScannerState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun AddContactsContent(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    contactCode: DatabaseRequestState<Bitmap>,
    onScannerStateChanged: (QrCodeScannerState) -> Unit,
    mainViewModel: MainViewModel
) {
    when(scannerState)
    {
        QrCodeScannerState.SHARE_CODE,
        QrCodeScannerState.SAVE -> {
            DisplayQrCode(
                modifier = modifier,
                scannerState = scannerState,
                contactCode = contactCode,
                onScannerStateChanged = {
                    newScannerState -> onScannerStateChanged(newScannerState)
                },
                mainViewModel = mainViewModel
            )
        }
        QrCodeScannerState.SCAN_CODE -> {
            QrCodeScanner(
                onCodeFound = {
                    code -> if(mainViewModel.setScannedContactDetails(code))
                    {
                        onScannerStateChanged(QrCodeScannerState.SAVE)
                    }
                }
            )
        }
    }
}

@Composable
fun DisplayQrCode(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    contactCode: DatabaseRequestState<Bitmap>,
    onScannerStateChanged: (QrCodeScannerState) -> Unit,
    mainViewModel: MainViewModel
) {
    var contactName by remember { mutableStateOf("") }

    if(contactCode is DatabaseRequestState.Success) {
        ContactQrCode(
            modifier = modifier,
            qrCode = contactCode.data
        )
    } else {
        CodeNotAvailableError()
    }

    if(scannerState == QrCodeScannerState.SAVE)
    {
        EditContactDialog(
            contactName = contactName,
            onContactNameChanged = { newContactName ->
                contactName = newContactName
                mainViewModel.updateNewContactName(newContactName)
            },
            title = stringResource(
                id = R.string.qr_code_scanned_successfully
            ),
            body = stringResource(
                id = R.string.save_contact_message
            ),
            dismissible = true,
            onDismissRequest = {
                mainViewModel.resetNewContactDetails()
                onScannerStateChanged(QrCodeScannerState.SHARE_CODE)
            },
            onConfirmClicked = {
                onScannerStateChanged(QrCodeScannerState.SHARE_CODE)
                mainViewModel.saveNewContact()
            },
            onDismissClicked = {
                mainViewModel.resetNewContactDetails()
                onScannerStateChanged(QrCodeScannerState.SHARE_CODE)
            }
        )
    }
}

