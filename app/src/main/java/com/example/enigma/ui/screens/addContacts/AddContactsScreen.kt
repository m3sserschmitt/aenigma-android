package com.example.enigma.ui.screens.addContacts

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
import androidx.compose.ui.res.painterResource
import com.example.enigma.R
import com.example.enigma.util.QrCodeScannerState
import com.example.enigma.viewmodels.MainViewModel

@Composable
fun AddContactsScreen(
    navigateToContactsScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    LaunchedEffect(key1 = true)
    {
        mainViewModel.generateCode()
    }

    var scannerState by remember { mutableStateOf(QrCodeScannerState.SHARE_CODE) }
    val contactCode by mainViewModel.contactQrCode.collectAsState()

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
                contactCode = contactCode,
                onScannerStateChanged = {
                    newScannerState -> scannerState = newScannerState
                },
                mainViewModel = mainViewModel
            )
        },
        floatingActionButton = {
            QrScannerFab(
                scannerState = scannerState,
                onClick = {
                    if(scannerState == QrCodeScannerState.SHARE_CODE)
                    {
                        scannerState = QrCodeScannerState.SCAN_CODE
                    } else if(scannerState == QrCodeScannerState.SCAN_CODE)
                    {
                        scannerState = QrCodeScannerState.SHARE_CODE
                    }
                }
            )
        }
    )
}

@Composable
fun QrScannerFab(
    scannerState: QrCodeScannerState,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            painter = if(scannerState == QrCodeScannerState.SHARE_CODE)
                painterResource(id = R.drawable.ic_qr_scanner)
            else
                painterResource(id = R.drawable.ic_qr_code),
            contentDescription = ""
        )
    }
}
