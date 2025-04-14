package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.util.RequestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadingDialog(
    state: RequestState<*>,
    onConfirmButtonClicked: () -> Unit
) {
    if (state !is RequestState.Idle) {
        val title = when (state) {
            is RequestState.Loading -> stringResource(R.string.please_wait)
            is RequestState.Success -> stringResource(R.string.request_successfully_completed)
            is RequestState.Error -> stringResource(R.string.request_completed_with_errors)
            else -> ""
        }
        val okButtonVisible = when (state) {
            is RequestState.Success,
            is RequestState.Error -> true
            else -> false
        }
        val spinnerVisible = when (state) {
            is RequestState.Loading -> true
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
                            text = stringResource(R.string.loading),
                            textStyle = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            textColor = MaterialTheme.colorScheme.onBackground
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
    state: RequestState<*>,
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
        state = RequestState.Loading,
        onConfirmButtonClicked = { }
    )
}

@Preview
@Composable
fun LoadingDialogSuccessPreview()
{
    LoadingDialog(
        state = RequestState.Success(true),
        onConfirmButtonClicked = { }
    )
}

@Preview
@Composable
fun LoadingDialogErrorPreview()
{
    LoadingDialog(
        state = RequestState.Error(Exception()),
        onConfirmButtonClicked = { }
    )
}
