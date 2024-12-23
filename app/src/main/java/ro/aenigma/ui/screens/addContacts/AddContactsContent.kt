package ro.aenigma.ui.screens.addContacts

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import ro.aenigma.models.SharedData
import ro.aenigma.ui.screens.common.LoadingDialog
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.SaveNewContactDialog
import ro.aenigma.ui.screens.common.UseLinkDialog
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.DatabaseRequestState
import ro.aenigma.util.QrCodeGenerator
import ro.aenigma.util.QrCodeScannerState

@Composable
fun AddContactsContent(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    qrCodeLabel: String,
    qrCode: DatabaseRequestState<Bitmap>,
    sharedDataCreate: DatabaseRequestState<CreatedSharedData>,
    sharedDataGet: DatabaseRequestState<SharedData>,
    onQrCodeFound: (String) -> Unit,
    onNewContactNameChanged: (String) -> Boolean,
    onSaveContact: () -> Unit,
    onSaveContactDismissed: () -> Unit,
    onCreateLinkClicked: () -> Unit,
    onGetLink: (String) -> Unit,
    onSharedDataConfirm: () ->  Unit
) {
    var link by remember { mutableStateOf("") }
    var useLinkDialogVisible by remember { mutableStateOf(false) }
    var createLinkDialogVisible by remember { mutableStateOf(false) }
    var saveContactDialogVisible by remember { mutableStateOf(false) }
    var useLinkLoadingDialogVisible by remember { mutableStateOf(false) }
    var createLinkLoadingDialogVisible by remember { mutableStateOf(false) }

    LoadingDialog(
        visible = useLinkLoadingDialogVisible,
        state = sharedDataGet,
        onConfirmButtonClicked = {
            saveContactDialogVisible = true
            useLinkLoadingDialogVisible = false
        }
    )

    LoadingDialog(
        visible = createLinkLoadingDialogVisible,
        state = sharedDataCreate,
        onConfirmButtonClicked = {
            createLinkDialogVisible = true
            createLinkLoadingDialogVisible = false
        }
    )

    SaveNewContactDialog(
        visible = scannerState == QrCodeScannerState.SAVE
                || (sharedDataGet is DatabaseRequestState.Success && saveContactDialogVisible),
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = {
            saveContactDialogVisible = false
            onSaveContact()
        },
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
        onTextChanged = { newLink ->
            link = newLink
            Patterns.WEB_URL.matcher(link).matches()
        },
        onConfirmClicked = {
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
                qrCodeLabel = qrCodeLabel,
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
    onCreateLinkClicked: () -> Unit,
    onUseLinkClicked: () -> Unit
) {
    when(qrCode) {
        is DatabaseRequestState.Success -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                QrCode(
                    qrCodeLabel = qrCodeLabel,
                    qrCode = qrCode.data
                )
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.background,
                    thickness = 36.dp
                )
                TextButton(
                    onClick = onCreateLinkClicked
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.create_link
                        ),
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
                TextButton(
                    onClick = onUseLinkClicked
                ) {
                    Text(
                        text = stringResource(
                            id = R.string.use_link
                        ),
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    )
                }
            }
        }
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
                onSaveContactDismissed = { },
                onCreateLinkClicked = { },
                sharedDataCreate = DatabaseRequestState.Idle,
                onSharedDataConfirm = { },
                onGetLink = { },
                sharedDataGet = DatabaseRequestState.Idle
            )
        }
    }
}
