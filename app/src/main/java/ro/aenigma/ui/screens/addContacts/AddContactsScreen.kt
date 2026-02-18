package ro.aenigma.ui.screens.addContacts

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.models.CreatedSharedDataDto
import ro.aenigma.models.ExportedContactDataDto
import ro.aenigma.models.QrCodeDto
import ro.aenigma.models.ServerInfoDto
import ro.aenigma.ui.navigation.Screens
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.ui.themes.ApplicationComposeDarkTheme
import ro.aenigma.util.RequestState
import ro.aenigma.util.QrCodeGenerator
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.viewmodels.MainViewModel

@Composable
fun AddContactsScreen(
    profileToShare: String,
    initialScannerState: QrCodeScannerState,
    navigateToContactsScreen: () -> Unit,
    mainViewModel: MainViewModel
) {
    var scannerState by remember { mutableStateOf(value = initialScannerState) }
    val qrCode by mainViewModel.qrCode.collectAsState()
    val sharedDataCreate by mainViewModel.sharedDataCreateResult.collectAsState()
    val importedContactDetails by mainViewModel.importedContactDetails.collectAsState()
    val floatingButtonVisible = profileToShare == Screens.ADD_CONTACT_SCREEN_SHARE_MY_CODE_ARG_VALUE
            && scannerState != QrCodeScannerState.SCAN_SERVER_INFO_CODE
    val context = LocalContext.current

    LaunchedEffect(key1 = true)
    {
        mainViewModel.generateCode(profileToShare)
    }

    AddContactsScreen(
        scannerState = scannerState,
        qrCode = qrCode,
        sharedDataCreate = sharedDataCreate,
        importedContactDetails = importedContactDetails,
        floatingButtonVisible = floatingButtonVisible,
        onScannerStateChanged = {
            newState -> scannerState = newState
        },
        onQrCodeFound = {
            scannedData ->
            mainViewModel.setScannedContactDetails(scannedData)
            scannerState = QrCodeScannerState.SAVE
        },
        onServerInfoQrCodeFound = { scannedData ->
            mainViewModel.setServerInfoScannedDetails(scannedData)
            navigateToContactsScreen()
        },
        onSaveContact = { name ->
            scannerState = QrCodeScannerState.SHARE_CODE
            mainViewModel.saveNewContact(name)
            Toast.makeText(context, context.getString(R.string.saved), Toast.LENGTH_SHORT).show()
            navigateToContactsScreen()
        },
        onSaveContactDismissed = {
            mainViewModel.resetContactChanges()
            scannerState = QrCodeScannerState.SHARE_CODE
        },
        onNewContactNameChanged = {
            newContactName -> newContactName.isNotBlank()
        },
        onCreateLinkClicked = { mainViewModel.createContactShareLink() },
        onGetLink = { url -> mainViewModel.openContactSharedData(url) },
        onSharedDataConfirm = { mainViewModel.resetContactChanges() },
        navigateToContactsScreen = navigateToContactsScreen
    )
}

@Composable
fun AddContactsScreen(
    scannerState: QrCodeScannerState,
    qrCode: RequestState<QrCodeDto>,
    sharedDataCreate: RequestState<CreatedSharedDataDto>,
    importedContactDetails: RequestState<ExportedContactDataDto>,
    floatingButtonVisible: Boolean,
    onScannerStateChanged: (QrCodeScannerState) -> Unit,
    onQrCodeFound: (ExportedContactDataDto) -> Unit,
    onServerInfoQrCodeFound: (ServerInfoDto) -> Unit,
    onSaveContact: (String) -> Unit,
    onSaveContactDismissed: () -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onCreateLinkClicked: () -> Unit,
    onGetLink: (String) -> Unit,
    onSharedDataConfirm: () -> Unit,
    navigateToContactsScreen: () -> Unit
) {
    Scaffold(
        topBar = {
            val isScanning =
                scannerState == QrCodeScannerState.SCAN_CODE || scannerState == QrCodeScannerState.SCAN_SERVER_INFO_CODE
            StandardAppBar(
                title = if (isScanning) {
                    stringResource(R.string.scan_qr_code)
                } else {
                    stringResource(R.string.add_contacts)
                },
                navigateBack = navigateToContactsScreen,
                transparent = isScanning
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
                onSaveContact = onSaveContact,
                onSaveContactDismissed = onSaveContactDismissed,
                onQrCodeFound = onQrCodeFound,
                onServerInfoQrCodeFound = onServerInfoQrCodeFound,
                onNewContactNameChanged = onNewContactNameChanged,
                onCreateLinkClicked = onCreateLinkClicked,
                onGetLink = onGetLink,
                onSharedDataConfirm = onSharedDataConfirm
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
        scannerState = QrCodeScannerState.SHARE_CODE,
        qrCode = RequestState.Success(
            QrCodeDto(
                QrCodeGenerator(
                    400,
                    400
                ).encodeAsBitmap("Congratulation, dude! You cracked the code!")!!, "John", true
            )
        ),
        sharedDataCreate = RequestState.Idle,
        importedContactDetails = RequestState.Idle,
        floatingButtonVisible = true,
        onNewContactNameChanged = { true },
        onQrCodeFound = { },
        onServerInfoQrCodeFound = { },
        onSaveContact = { },
        onScannerStateChanged = { },
        onSaveContactDismissed = { },
        navigateToContactsScreen = { },
        onCreateLinkClicked = { },
        onGetLink = { },
        onSharedDataConfirm = { }
    )
}

@Preview
@Composable
fun AddContactsScreenDarkPreview() {
    ApplicationComposeDarkTheme {
        AddContactsScreenPreview()
    }
}
