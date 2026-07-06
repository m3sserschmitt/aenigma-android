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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionRequiredDialog(
    visible: Boolean,
    bodyText: String,
    rememberDecisionCheckboxVisible: Boolean,
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: (Boolean) -> Unit
) {
    if (visible) {
        var rememberDecisionChecked by remember { mutableStateOf(false) }

        BasicAlertDialog(
            onDismissRequest = { }
        ) {
            DialogContentTemplate(
                title = stringResource(id = R.string.permission_required),
                body = bodyText,
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (rememberDecisionCheckboxVisible) {
                            Checkbox(
                                checked = rememberDecisionChecked,
                                onCheckedChange = {
                                    rememberDecisionChecked = !rememberDecisionChecked
                                })
                            Text(
                                text = stringResource(id = R.string.remember_decision),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                },
                onPositiveButtonClicked = onPositiveButtonClicked,
                onNegativeButtonClicked = {
                    onNegativeButtonClicked(rememberDecisionChecked)
                },
                positiveButtonText = stringResource(id = R.string.settings),
                negativeButtonText = stringResource(id = R.string.do_not_allow)
            )
        }
    }
}

@Composable
fun NotificationsPermissionRequiredDialog(
    visible: Boolean,
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: (Boolean) -> Unit
) {
    PermissionRequiredDialog(
        visible = visible,
        rememberDecisionCheckboxVisible = true,
        bodyText = stringResource(id = R.string.notifications_permission_required),
        onPositiveButtonClicked = onPositiveButtonClicked,
        onNegativeButtonClicked = onNegativeButtonClicked
    )
}

@Composable
fun CameraPermissionRequiredDialog(
    visible: Boolean,
    onPositiveButtonClicked: () -> Unit,
    onNegativeButtonClicked: (Boolean) -> Unit
) {
    PermissionRequiredDialog(
        visible = visible,
        bodyText = stringResource(id = R.string.camera_permission_required_to_scan),
        rememberDecisionCheckboxVisible = false,
        onPositiveButtonClicked = onPositiveButtonClicked,
        onNegativeButtonClicked = onNegativeButtonClicked
    )
}

@Preview
@Composable
fun NotificationsPermissionRequiredDialogPreview()
{
    NotificationsPermissionRequiredDialog(
        visible = true,
        onNegativeButtonClicked = {},
        onPositiveButtonClicked = {}
    )
}

@Preview
@Composable
fun PermissionRequiredDialogPreview()
{
    PermissionRequiredDialog(
        visible = true,
        rememberDecisionCheckboxVisible = true,
        onPositiveButtonClicked = {},
        onNegativeButtonClicked = {},
        bodyText = stringResource(id = R.string.notifications_permission_required)
    )
}
