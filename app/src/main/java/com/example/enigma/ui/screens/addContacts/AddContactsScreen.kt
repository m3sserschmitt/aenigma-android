package com.example.enigma.ui.screens.addContacts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.enigma.R
import com.example.enigma.models.CreatedSharedData
import com.example.enigma.ui.navigation.Screens
import com.example.enigma.util.DatabaseRequestState
import com.example.enigma.util.QrCodeGenerator
import com.example.enigma.util.QrCodeScannerState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun AddContactsScreen(
    profileToShare: String,
    navigateToContactsScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    var scannerState by remember { mutableStateOf(QrCodeScannerState.SHARE_CODE) }
    val qrCode by mainViewModel.qrCode.collectAsState()
    val sharedData by mainViewModel.sharedDataCreateResult.collectAsState()
    val qrCodeLabel by mainViewModel.qrCodeLabel.collectAsState()
    val floatingButtonVisible = profileToShare == Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE
    val context = LocalContext.current

    LaunchedEffect(key1 = true)
    {
        mainViewModel.generateCode(profileToShare)
    }

    AddContactsScreen(
        scannerState = scannerState,
        qrCode = qrCode,
        sharedData = sharedData,
        qrCodeLabel = qrCodeLabel,
        floatingButtonVisible = floatingButtonVisible,
        onScannerStateChanged = {
            newState -> scannerState = newState
        },
        onQrCodeFound = {
            scannedData -> if(mainViewModel.setScannedContactDetails(scannedData))
            scannerState = QrCodeScannerState.SAVE
        },
        onSaveContact = {
            scannerState = QrCodeScannerState.SHARE_CODE
            mainViewModel.saveContactChanges()
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            navigateToContactsScreen()
        },
        onSaveContactDismissed = {
            mainViewModel.cleanupContactChanges()
            scannerState = QrCodeScannerState.SHARE_CODE
        },
        onNewContactNameChanged = {
            newContactName -> mainViewModel.setNewContactName(newContactName)
        },
        onCreateLinkClicked = {
            mainViewModel.createContactShareLink()
        },
        onSharedDataConfirm = {
            mainViewModel.cleanupContactChanges()
        },
        navigateToContactsScreen = navigateToContactsScreen
    )
}

@Composable
fun AddContactsScreen(
    scannerState: QrCodeScannerState,
    qrCode: DatabaseRequestState<Bitmap>,
    sharedData: DatabaseRequestState<CreatedSharedData>,
    qrCodeLabel: String,
    floatingButtonVisible: Boolean,
    onScannerStateChanged: (QrCodeScannerState) -> Unit,
    onQrCodeFound: (String) -> Unit,
    onSaveContact: () -> Unit,
    onSaveContactDismissed: () -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onCreateLinkClicked: () -> Unit,
    onSharedDataConfirm: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    Scaffold (
        topBar = {
            AddContactsAppBar(
                navigateToContactsScreen = navigateToContactsScreen
            )
        },
        content = { paddingValues ->
            AddContactsContent(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding()
                    ),
                scannerState = scannerState,
                qrCode = qrCode,
                sharedData = sharedData,
                qrCodeLabel = qrCodeLabel,
                onSaveContact = onSaveContact,
                onSaveContactDismissed = onSaveContactDismissed,
                onQrCodeFound = onQrCodeFound,
                onNewContactNameChanged = onNewContactNameChanged,
                onCreateLinkClicked = onCreateLinkClicked,
                onSharedDataConfirm = onSharedDataConfirm
            )
        },
        floatingActionButton = {
            QrScannerFab(
                scannerState = scannerState,
                visible = floatingButtonVisible,
                onClick = {
                    if(scannerState == QrCodeScannerState.SHARE_CODE)
                    {
                        onScannerStateChanged(QrCodeScannerState.SCAN_CODE)
                    }
                    else if(scannerState == QrCodeScannerState.SCAN_CODE)
                    {
                        onScannerStateChanged(QrCodeScannerState.SHARE_CODE)
                    }
                }
            )
        }
    )
}

@Composable
fun QrScannerFab(
    visible: Boolean,
    scannerState: QrCodeScannerState,
    onClick: () -> Unit
) {
    if(visible) {
        FloatingActionButton(
            onClick = {
                onClick()
            }
        ) {
            Icon(
                painter = if (scannerState == QrCodeScannerState.SHARE_CODE)
                    painterResource(id = R.drawable.ic_qr_scanner)
                else
                    painterResource(id = R.drawable.ic_qr_code),
                contentDescription = ""
            )
        }
    }
}

@SuppressLint("UseCompatLoadingForDrawables")
@Preview
@Composable
fun AddContactsScreenPreview()
{
    val bitmap = QrCodeGenerator(400, 400).encodeAsBitmap("Hello world!")
    if(bitmap != null) {
        AddContactsScreen(
            scannerState = QrCodeScannerState.SHARE_CODE,
            qrCode = DatabaseRequestState.Success(bitmap),
            sharedData = DatabaseRequestState.Idle,
            qrCodeLabel = "John",
            floatingButtonVisible = true,
            onNewContactNameChanged = { true },
            onQrCodeFound = { },
            onSaveContact = { },
            onScannerStateChanged = { },
            onSaveContactDismissed = { },
            navigateToContactsScreen = { },
            onCreateLinkClicked = { },
            onSharedDataConfirm = { }
        )
    }
}
