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

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import ro.aenigma.R
import ro.aenigma.models.CreatedSharedDataDto
import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.QrCodeDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.ContextExtensions.shareQrCode
import ro.aenigma.util.ContextExtensions.showFailedToShareToast
import ro.aenigma.util.RequestState
import ro.aenigma.util.QrCodeGenerator
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun AddContactsScreen(
    profileToShare: String?,
    initialScannerState: QrCodeScannerState,
    navigateBack: () -> Unit,
    onForwardUri: (String) -> Unit = { },
    navigateToRoot: () -> Unit = { },
    mainViewModel: MainViewModel
) {
    var scannerState by remember { mutableStateOf(value = initialScannerState) }
    val qrCode by mainViewModel.qrCode.collectAsState()
    val ephemeralLinksPreference by mainViewModel.ephemeralLinksPreference.collectAsState()
    val sharedDataCreate by mainViewModel.sharedDataCreateResult.collectAsState()
    val importedContactDetails by mainViewModel.importedContactDetails.collectAsState()
    val floatingButtonVisible = profileToShare == null
            && scannerState != QrCodeScannerState.SCAN_SERVER_INFO_CODE
    val uri by mainViewModel.uri.collectAsState()
    var isContactImport by remember(key1 = uri) { mutableStateOf(uri != null) }

    LaunchedEffect(key1 = true) {
        mainViewModel.generateCode(profileToShare)
    }

    LaunchedEffect(key1 = uri) {
        if (uri != null) {
            mainViewModel.openContactSharedData(uri.toString())
        }
    }

    AddContactsScreen(
        scannerState = scannerState,
        qrCode = qrCode,
        sharedDataCreate = sharedDataCreate,
        importedContactDetails = importedContactDetails,
        isContactImport = isContactImport,
        floatingButtonVisible = floatingButtonVisible,
        ephemeralLinksPreference = ephemeralLinksPreference,
        onResetUsernameClicked = { mainViewModel.resetUserName() },
        onEphemeralLinksPreferenceChanged = { ephemeralLinksPreference ->
            mainViewModel.ephemeralLinksPreferenceChanged(ephemeralLinksPreference)
        },
        onScannerStateChanged = { newState ->
            scannerState = newState
        },
        onQrCodeFound = { scannedData ->
            mainViewModel.setScannedContactDetails(scannedData)
            scannerState = QrCodeScannerState.SAVE
        },
        onServerInfoQrCodeFound = { scannedData ->
            mainViewModel.setServerInfoScannedDetails(scannedData)
            navigateBack()
        },
        onSaveContact = { name ->
            scannerState = QrCodeScannerState.SHARE_CODE
            mainViewModel.saveNewContact(name)
            mainViewModel.setUri(null)
            navigateToRoot()
        },
        onSaveContactDismissed = {
            mainViewModel.resetContactChanges()
            scannerState = QrCodeScannerState.SHARE_CODE
            mainViewModel.setUri(null)
        },
        onNewContactNameChanged = { newContactName ->
            newContactName.isNotBlank()
        },
        onCreateLinkClicked = { mainViewModel.createContactShareLink() },
        onGetLink = { url -> mainViewModel.openContactSharedData(url) },
        onSharedDataConfirm = { mainViewModel.resetContactChanges() },
        onForwardUri = onForwardUri,
        navigateBack = navigateBack
    )
}

@Composable
fun AddContactsScreen(
    scannerState: QrCodeScannerState = QrCodeScannerState.SHARE_CODE,
    qrCode: RequestState<QrCodeDto> = RequestState.Idle,
    sharedDataCreate: RequestState<CreatedSharedDataDto> = RequestState.Idle,
    importedContactDetails: RequestState<ExportedContactDataDto> = RequestState.Idle,
    ephemeralLinksPreference: Boolean = false,
    moreOptionsMenuExpanded: Boolean = false,
    isContactImport: Boolean = false,
    floatingButtonVisible: Boolean = true,
    onResetUsernameClicked: () -> Unit = { },
    onEphemeralLinksPreferenceChanged: (Boolean) -> Unit = { },
    onScannerStateChanged: (QrCodeScannerState) -> Unit = { },
    onQrCodeFound: (ExportedContactDataDto) -> Unit = { },
    onServerInfoQrCodeFound: (ServerInfoDto) -> Unit = { },
    onSaveContact: (String) -> Unit = { },
    onSaveContactDismissed: () -> Unit = { },
    onNewContactNameChanged: (String) -> Boolean  = { true },
    onCreateLinkClicked: () -> Unit = { },
    onGetLink: (String) -> Unit = { },
    onSharedDataConfirm: () -> Unit = { },
    onForwardUri: (String) -> Unit = { },
    navigateBack: () -> Unit = { }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AddContactsAppBar(
                moreOptionsMenuExpanded = moreOptionsMenuExpanded,
                scannerState = scannerState,
                ephemeralLinksPreference = ephemeralLinksPreference,
                onResetUsernameClicked = onResetUsernameClicked,
                onEphemeralLinksPreferenceChanged = onEphemeralLinksPreferenceChanged,
                onExportQrCodeClicked = {
                    if (qrCode is RequestState.Success) {
                        coroutineScope.launch {
                            if (!context.shareQrCode(bitmap = qrCode.data.code)) {
                                context.showFailedToShareToast()
                            }
                        }
                    }
                },
                navigateBack = navigateBack
            )
        },
        content = { paddingValues ->
            AddContactsContent(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding(),
                    ),
                scannerState = scannerState,
                qrCode = qrCode,
                sharedDataCreate = sharedDataCreate,
                importedContactDetails = importedContactDetails,
                isContactImport = isContactImport,
                onSaveContact = onSaveContact,
                onSaveContactDismissed = onSaveContactDismissed,
                onQrCodeFound = onQrCodeFound,
                onServerInfoQrCodeFound = onServerInfoQrCodeFound,
                onNewContactNameChanged = onNewContactNameChanged,
                onCreateLinkClicked = onCreateLinkClicked,
                onGetLink = onGetLink,
                onSharedDataConfirm = onSharedDataConfirm,
                onForwardUri = onForwardUri
            )
        },
        floatingActionButton = {
            QrScannerFab(
                scannerState = scannerState,
                visible = floatingButtonVisible,
                onClick = {
                    if (scannerState == QrCodeScannerState.SHARE_CODE) {
                        onScannerStateChanged(QrCodeScannerState.SCAN_CODE)
                    } else if (scannerState == QrCodeScannerState.SCAN_CODE) {
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

@Preview
@Composable
fun AddContactsScreenPreview() {
    AddContactsScreen(
        qrCode = RequestState.Success(
            QrCodeDto(
                QrCodeGenerator(
                    400,
                    400
                ).encodeAsBitmap("Congratulation, dude! You cracked the code!")!!, "John", true
            )
        )
    )
}

@Preview
@Composable
fun AddContactsScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        AddContactsScreenPreview()
    }
}
