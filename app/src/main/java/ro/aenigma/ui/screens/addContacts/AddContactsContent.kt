package ro.aenigma.ui.screens.addContacts

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.QrCodeDto
import ro.aenigma.models.SharedData
import ro.aenigma.ui.screens.common.LoadingDialog
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.SaveNewContactDialog
import ro.aenigma.ui.screens.common.UseLinkDialog
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.RequestState
import ro.aenigma.util.QrCodeGenerator
import ro.aenigma.util.QrCodeScannerState

@Composable
fun AddContactsContent(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    qrCode: RequestState<QrCodeDto>,
    sharedDataCreate: RequestState<CreatedSharedData>,
    sharedDataGet: RequestState<SharedData>,
    importedContactDetails: ExportedContactData?,
    onQrCodeFound: (ExportedContactData) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onSaveContact: (String) -> Unit,
    onSaveContactDismissed: () -> Unit,
    onCreateLinkClicked: () -> Unit,
    onGetLink: (String) -> Unit,
    onSharedDataConfirm: () ->  Unit
) {
    var useLinkDialogVisible by remember { mutableStateOf(false) }
    var createLinkDialogVisible by remember { mutableStateOf(false) }
    var saveContactDialogVisible by remember { mutableStateOf(false) }
    var useLinkLoadingDialogVisible by remember { mutableStateOf(false) }
    var createLinkLoadingDialogVisible by remember { mutableStateOf(false) }

    LoadingDialog(
        visible = useLinkLoadingDialogVisible,
        state = sharedDataGet,
        onConfirmButtonClicked = {
            if(sharedDataGet is RequestState.Error)
            {
                onSharedDataConfirm()
            }
            else
            {
                saveContactDialogVisible = true
            }
            useLinkLoadingDialogVisible = false
        }
    )

    LoadingDialog(
        visible = createLinkLoadingDialogVisible,
        state = sharedDataCreate,
        onConfirmButtonClicked = {
            if(sharedDataCreate is RequestState.Error)
            {
                onSharedDataConfirm()
            }
            else
            {
                createLinkDialogVisible = true
            }
            createLinkLoadingDialogVisible = false
        }
    )

    SaveNewContactDialog(
        visible = scannerState == QrCodeScannerState.SAVE
                || (sharedDataGet is RequestState.Success && saveContactDialogVisible),
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            saveContactDialogVisible = false
            onSaveContact(name)
        },
        initialName = importedContactDetails?.userName ?: "",
        onDismissClicked = {
            saveContactDialogVisible = false
            onSaveContactDismissed()
        },
    )

    CreateLinkDialog(
        visible = createLinkDialogVisible,
        sharedData = sharedDataCreate,
        onConfirmButtonClick = {
            onSharedDataConfirm()
            createLinkDialogVisible = false
        }
    )

    UseLinkDialog(
        visible = useLinkDialogVisible,
        onConfirmClicked = { link ->
            onGetLink(link)
            useLinkDialogVisible = false
            useLinkLoadingDialogVisible = true
        },
        onDismissClicked = { useLinkDialogVisible = false }
    )

    when (scannerState) {
        QrCodeScannerState.SHARE_CODE,
        QrCodeScannerState.SAVE -> {
            DisplayQrCode(
                modifier = modifier,
                qrCode = qrCode,
                onCreateLinkClicked = {
                    createLinkLoadingDialogVisible = true
                    onCreateLinkClicked()
                },
                onUseLinkClicked = {
                    useLinkDialogVisible = true
                }
            )
        }
        QrCodeScannerState.SCAN_CODE -> {
            QrCodeScanner<ExportedContactData>(
                onQrCodeFound = { data ->
                    onQrCodeFound(data)
                }
            )
        }
    }
}

@Composable
fun DisplayQrCode(
    modifier: Modifier = Modifier,
    qrCode: RequestState<QrCodeDto>,
    onCreateLinkClicked: () -> Unit,
    onUseLinkClicked: () -> Unit
) {
    when (qrCode) {
        is RequestState.Success -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QrCode(
                    qrCode = qrCode.data
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 12.dp
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(.5f),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(
                            onClick = onCreateLinkClicked
                        ) {
                            Text(
                                text = stringResource(
                                    id = R.string.create_link
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if(qrCode.data.isOwnCode) {
                            TextButton(
                                onClick = onUseLinkClicked,
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.use_link
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        is RequestState.Error -> CodeNotAvailableError()
        is RequestState.Loading -> LoadingScreen()
        is RequestState.Idle -> {}
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
                qrCode = RequestState.Success(QrCodeDto(bitmap, "John", false)),
                importedContactDetails = ExportedContactData(
                    guardAddress = "",
                    guardHostname = "",
                    userName = "",
                    publicKey = ""
                ),
                onQrCodeFound = { },
                onNewContactNameChanged = { true },
                onSaveContact = { },
                onSaveContactDismissed = { },
                onCreateLinkClicked = { },
                sharedDataCreate = RequestState.Idle,
                onSharedDataConfirm = { },
                onGetLink = { },
                sharedDataGet = RequestState.Idle
            )
        }
    }
}
