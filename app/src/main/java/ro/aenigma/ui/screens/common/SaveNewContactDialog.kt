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

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@Composable
fun SaveNewContactDialog(
    visible: Boolean,
    initialName: String = "",
    onContactNameChanged: (String) -> Boolean,
    onConfirmClicked: (String) -> Unit,
    onDismissClicked: () -> Unit
) {
    if(visible) {
        TextInputDialog(
            onTextChanged = onContactNameChanged,
            title = stringResource(
                id = R.string.contact_details_successfully_retrieved
            ),
            body = stringResource(
                id = R.string.save_contact_message
            ),
            initialText = initialName,
            placeholderText = stringResource(id = R.string.contact_name),
            onConfirmClicked = onConfirmClicked,
            onDismissClicked = onDismissClicked
        )
    }
}

@Preview
@Composable
fun SaveNewContactDialogPreview()
{
    SaveNewContactDialog(
        visible = true,
        initialName = "John",
        onContactNameChanged = { true },
        onConfirmClicked = { },
        onDismissClicked = { },
    )
}
