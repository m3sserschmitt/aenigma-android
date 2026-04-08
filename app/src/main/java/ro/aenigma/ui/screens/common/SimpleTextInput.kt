package ro.aenigma.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SimpleOutlineTextInput(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    singleLine: Boolean = true,
    isError: Boolean = false,
    onValueChanged: (String) -> Unit = { },
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors().copy(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            errorContainerColor = MaterialTheme.colorScheme.background,
        ),
        isError = isError,
        onValueChange = onValueChanged,
        label = {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = singleLine
    )
}

@Composable
fun SimpleTextInput(
    modifier: Modifier = Modifier,
    value: String,
    placeholder: String,
    singleLine: Boolean = true,
    onValueChanged: (String) -> Unit = { },
) {
    TextField(
        modifier = modifier,
        value = value,
        textStyle = MaterialTheme.typography.bodyMedium,
        colors = TextFieldDefaults.colors().copy(
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedContainerColor = MaterialTheme.colorScheme.background,
            errorContainerColor = MaterialTheme.colorScheme.background,
        ),
        onValueChange = onValueChanged,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .25f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        singleLine = singleLine
    )
}
