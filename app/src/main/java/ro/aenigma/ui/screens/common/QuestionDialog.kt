package ro.aenigma.ui.screens.common

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionDialog(
    title: String,
    question: String,
    content: @Composable () -> Unit = { },
    negativeButtonText: String = stringResource(id = R.string.dismiss),
    positiveButtonText: String = stringResource(id = R.string.confirm),
    onPositiveButtonClicked: () -> Unit = { },
    onNegativeButtonClicked: () -> Unit = { },
) {
    BasicAlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnClickOutside = false
        )
    ) {
        DialogContentTemplate(
            content = content,
            title = title,
            body = question,
            dismissible = true,
            onPositiveButtonClicked = onPositiveButtonClicked,
            onNegativeButtonClicked = onNegativeButtonClicked,
            negativeButtonText = negativeButtonText,
            positiveButtonText = positiveButtonText
        )
    }
}
