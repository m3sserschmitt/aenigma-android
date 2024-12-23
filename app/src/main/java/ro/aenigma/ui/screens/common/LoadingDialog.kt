package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.util.DatabaseRequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(
    state: DatabaseRequestState<*>,
    onConfirmButtonClicked: () -> Unit
) {
    if (state !is DatabaseRequestState.Idle) {
        val title = when (state) {
            is DatabaseRequestState.Loading -> stringResource(R.string.please_wait)
            is DatabaseRequestState.Success -> stringResource(R.string.request_successfully_completed)
            is DatabaseRequestState.Error -> stringResource(R.string.request_completed_with_errors)
            else -> ""
        }
        val okButtonVisible = when (state) {
            is DatabaseRequestState.Success,
            is DatabaseRequestState.Error -> true
            else -> false
        }
        val spinnerVisible = when (state) {
            is DatabaseRequestState.Loading -> true
            else -> false
        }
        BasicAlertDialog(
            onDismissRequest = {}
        ) {
            DialogContentTemplate(
                title = title,
                body = "",
                content = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        IndeterminateCircularIndicator(
                            visible = spinnerVisible,
                            text = stringResource(R.string.loading)
                        )
                    }
                },
                dismissible = false,
                positiveButtonVisible = okButtonVisible,
                positiveButtonText = stringResource(R.string.ok),
                onPositiveButtonClicked = onConfirmButtonClicked,
                onNegativeButtonClicked = {}
            )
        }
    }
}

@Composable
fun LoadingDialog(
    visible: Boolean,
    state: DatabaseRequestState<*>,
    onConfirmButtonClicked: () -> Unit)
{
    if(visible)
    {
        LoadingDialog(
            state = state,
            onConfirmButtonClicked = onConfirmButtonClicked
        )
    }
}

@Preview
@Composable
fun LoadingDialogPreview()
{
    LoadingDialog(
        state = DatabaseRequestState.Loading,
        onConfirmButtonClicked = { }
    )
}

@Preview
@Composable
fun LoadingDialogSuccessPreview()
{
    LoadingDialog(
        state = DatabaseRequestState.Success(true),
        onConfirmButtonClicked = { }
    )
}

@Preview
@Composable
fun LoadingDialogErrorPreview()
{
    LoadingDialog(
        state = DatabaseRequestState.Error(Exception()),
        onConfirmButtonClicked = { }
    )
}
