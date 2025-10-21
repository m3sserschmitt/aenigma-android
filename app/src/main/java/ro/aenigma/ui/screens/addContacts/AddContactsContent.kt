package ro.aenigma.ui.screens.addContacts

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.CreatedSharedData
import ro.aenigma.models.ExportedContactData
import ro.aenigma.models.QrCodeDto
import ro.aenigma.ui.screens.common.LoadingDialog
import ro.aenigma.ui.screens.common.LoadingScreen
import ro.aenigma.ui.screens.common.SaveNewContactDialog
import ro.aenigma.ui.screens.common.UseLinkDialog
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.QrCodeGenerator
import ro.aenigma.util.QrCodeScannerState
import ro.aenigma.util.RequestState

@Composable
fun AddContactsContent(
    modifier: Modifier = Modifier,
    scannerState: QrCodeScannerState,
    qrCode: RequestState<QrCodeDto>,
    sharedDataCreate: RequestState<CreatedSharedData>,
    importedContactDetails: RequestState<ExportedContactData>,
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
        state = importedContactDetails,
        onConfirmButtonClicked = {
            if(importedContactDetails is RequestState.Error)
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

    val requestSuccessful = importedContactDetails is RequestState.Success
    val initialName = if(requestSuccessful) importedContactDetails.data.name ?: "" else ""
    SaveNewContactDialog(
        visible = scannerState == QrCodeScannerState.SAVE
                || (requestSuccessful && saveContactDialogVisible),
        onContactNameChanged = onNewContactNameChanged,
        onConfirmClicked = { name ->
            saveContactDialogVisible = false
            onSaveContact(name)
        },
        initialName = initialName,
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
            val cfg = LocalConfiguration.current
            val isPortrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT
            if (isPortrait) {
                DisplayPortraitQrCode(
                    modifier = modifier,
                    qrCode = qrCode.data,
                    onCreateLinkClicked = onCreateLinkClicked,
                    onUseLinkClicked = onUseLinkClicked
                )
            } else {
                DisplayLandscapeQrCode(
                    modifier = modifier,
                    qrCode = qrCode.data,
                    onCreateLinkClicked = onCreateLinkClicked,
                    onUseLinkClicked = onUseLinkClicked
                )
            }
        }

        is RequestState.Error -> CodeNotAvailableError(modifier)
        is RequestState.Loading -> LoadingScreen(modifier)
        is RequestState.Idle -> {}
    }
}

@Composable
fun ShareActionButtons(
    isOwnCode: Boolean,
    onCreateLinkClicked: () -> Unit,
    onUseLinkClicked: () -> Unit
) {
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
                        id = R.string.share_link
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if(isOwnCode) {
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

@Composable
fun DisplayPortraitQrCode(
    modifier: Modifier = Modifier,
    qrCode: QrCodeDto,
    onCreateLinkClicked: () -> Unit,
    onUseLinkClicked: () -> Unit
) {
    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.background
        ).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 28.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(
                id = R.string.qr_code_caption
            )
        )
        PortraitQrCode(
            qrCode = qrCode
        )
        Text(
            modifier = Modifier.alpha(.75f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            text = qrCode.label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        ShareActionButtons(
            isOwnCode = qrCode.isOwnCode,
            onCreateLinkClicked = onCreateLinkClicked,
            onUseLinkClicked = onUseLinkClicked
        )
    }
}

@Composable
fun DisplayLandscapeQrCode(
    modifier: Modifier = Modifier,
    qrCode: QrCodeDto,
    onCreateLinkClicked: () -> Unit,
    onUseLinkClicked: () -> Unit
) {
    Row(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.background
        ).fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LandscapeQrCode(
            qrCode = qrCode
        )
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 28.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyLarge,
                text = stringResource(
                    id = R.string.qr_code_caption
                )
            )
            ShareActionButtons(
                isOwnCode = qrCode.isOwnCode,
                onCreateLinkClicked = onCreateLinkClicked,
                onUseLinkClicked = onUseLinkClicked
            )
            Text(
                modifier = Modifier.alpha(.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                text = qrCode.label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
        }
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
                importedContactDetails = RequestState.Idle,
                onQrCodeFound = { },
                onNewContactNameChanged = { true },
                onSaveContact = { },
                onSaveContactDismissed = { },
                onCreateLinkClicked = { },
                sharedDataCreate = RequestState.Idle,
                onSharedDataConfirm = { },
                onGetLink = { },
            )
        }
    }
}
