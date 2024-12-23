package ro.aenigma.ui.screens.common

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorEstablishingConnectionDialog(
    visible: Boolean,
    onRetryNowClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if(visible) {
        BasicAlertDialog(onDismissRequest = onDismissClicked) {
            DialogContentTemplate(
                title = stringResource(id = R.string.connection_failed),
                body = stringResource(id = R.string.connection_failed_reason),
                content = {},
                onNegativeButtonClicked = onDismissClicked,
                onPositiveButtonClicked = onRetryNowClicked,
                positiveButtonText = stringResource(id = R.string.retry_now)
            )
        }
    }
}

@Preview
@Composable
fun ErrorEstablishingConnectionDialogPreview()
{
    ErrorEstablishingConnectionDialog(
        visible = true,
        onRetryNowClicked = {},
        onDismissClicked = {}
    )
}
