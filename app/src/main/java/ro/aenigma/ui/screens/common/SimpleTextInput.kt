/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
