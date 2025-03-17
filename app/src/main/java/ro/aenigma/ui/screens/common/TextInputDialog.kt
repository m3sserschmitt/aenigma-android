package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputDialog(
    title: String,
    body: String,
    placeholderText: String,
    dismissible: Boolean = true,
    initialText: String = "",
    onTextChanged: (String) -> Boolean,
    onConfirmClicked: (String) -> Unit,
    onDismissClicked: () -> Unit
) {
    var isValidationError by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(initialText) }

    BasicAlertDialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnClickOutside = false
        )
    ) {
        DialogContentTemplate(
            content = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = text,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = OutlinedTextFieldDefaults.colors().copy(
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        errorContainerColor = MaterialTheme.colorScheme.background,
                    ),
                    isError = isValidationError,
                    onValueChange = { newValue ->
                        isValidationError = !onTextChanged(newValue)
                        text = newValue
                    },
                    label = {
                        Text(
                            text = placeholderText,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    singleLine = true
                )
            },
            title = title,
            body = body,
            dismissible = dismissible,
            onPositiveButtonClicked = {
                isValidationError = isValidationError || text.isEmpty()
                if (!isValidationError) {
                    onConfirmClicked(text)
                    text = ""
                }
            },
            onNegativeButtonClicked = {
                onDismissClicked()
                text = ""
            }
        )
    }
}

@Composable
fun DialogContentTemplate(
    modifier: Modifier = Modifier,
    title: String,
    body: String = "",
    content: @Composable () -> Unit,
    dismissible: Boolean = true,
    positiveButtonVisible: Boolean = true,
    negativeButtonText: String = stringResource(id = R.string.dismiss),
    positiveButtonText: String = stringResource(id = R.string.confirm),
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = modifier.padding(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (body.isNotEmpty()) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = body,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                if (dismissible) {
                    OutlinedButton(
                        modifier = Modifier.padding(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors().copy(
                            containerColor = MaterialTheme.colorScheme.background
                        ),
                        onClick = {
                            onNegativeButtonClicked()
                        }
                    ) {
                        Text(
                            text = negativeButtonText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                if (positiveButtonVisible) {
                    Button(
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(4.dp),
                        onClick = {
                            onPositiveButtonClicked()
                        }
                    ) {
                        Text(
                            text = positiveButtonText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
fun TextInputDialogPreview()
{
    TextInputDialog(
        title = stringResource(
            id = R.string.contact_details_successfully_retrieved
        ),
        body = stringResource(
            id = R.string.save_contact_message
        ),
        onTextChanged = { true },
        onConfirmClicked = { },
        onDismissClicked = { },
        placeholderText = ""
    )
}
