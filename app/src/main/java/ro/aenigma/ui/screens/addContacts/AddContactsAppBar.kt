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

package ro.aenigma.ui.screens.addContacts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.ui.screens.common.BasicDropDownMenuItem
import ro.aenigma.ui.screens.common.BasicDropdownMenu
import ro.aenigma.ui.screens.common.BasicDropdownMenuItem
import ro.aenigma.ui.screens.common.DropdownMenuSwitch
import ro.aenigma.ui.screens.common.StandardAppBar
import ro.aenigma.util.QrCodeScannerState

@Composable
fun AddContactsAppBar(
    scannerState: QrCodeScannerState = QrCodeScannerState.SHARE_CODE,
    moreOptionsMenuExpanded: Boolean = false,
    ephemeralLinksPreference: Boolean = false,
    onResetUsernameClicked: () -> Unit = { },
    onEphemeralLinksPreferenceChanged: (Boolean) -> Unit = { },
    onExportQrCodeClicked: () -> Unit = { },
    navigateBack: () -> Unit = { },
) {
    val isScanning =
        scannerState == QrCodeScannerState.SCAN_CODE || scannerState == QrCodeScannerState.SCAN_SERVER_INFO_CODE
    StandardAppBar(
        title = if (isScanning) {
            stringResource(R.string.scan_qr_code)
        } else {
            stringResource(R.string.add_contacts)
        },
        navigateBack = navigateBack,
        transparent = isScanning,
        actions = {
            MoreActions(
                expanded = moreOptionsMenuExpanded,
                onResetUsernameClicked = onResetUsernameClicked,
                ephemeralLinksPreference = ephemeralLinksPreference,
                onEphemeralLinksPreferenceChanged = onEphemeralLinksPreferenceChanged,
                onExportQrCodeClicked = onExportQrCodeClicked
            )
        }
    )
}

@Composable
fun MoreActions(
    expanded: Boolean = false,
    ephemeralLinksPreference: Boolean = false,
    onResetUsernameClicked: () -> Unit = { },
    onEphemeralLinksPreferenceChanged: (Boolean) -> Unit = { },
    onExportQrCodeClicked: () -> Unit = { }
) {
    var isExpanded by remember(key1 = expanded) { mutableStateOf(expanded) }
    BasicDropdownMenu(
        expanded = isExpanded,
        onToggle = { value -> isExpanded = value }
    ) {
        EphemeralLinksSwitch(
            ephemeralLinkPreference = ephemeralLinksPreference,
            onEphemeralLinkPreferenceChanged = onEphemeralLinksPreferenceChanged
        )
        BasicDropdownMenuItem(
            painter = painterResource(id = R.drawable.ic_qr_code),
            contentDescription = stringResource(id = R.string.export_qr_code),
            text = stringResource(id = R.string.export_qr_code),
            onClick = {
                onExportQrCodeClicked()
                isExpanded = false
            }
        )
        BasicDropDownMenuItem(
            imageVector = Icons.Filled.AccountCircle,
            contentDescription = stringResource(id = R.string.reset_username),
            text = stringResource(id = R.string.reset_username),
            onClick = {
                onResetUsernameClicked()
                isExpanded = false
            }
        )
    }
}

@Composable
fun EphemeralLinksSwitch(
    ephemeralLinkPreference: Boolean = false,
    onEphemeralLinkPreferenceChanged: (Boolean) -> Unit = { }
) {
    DropdownMenuSwitch(
        value = ephemeralLinkPreference,
        isActive = ephemeralLinkPreference,
        text = stringResource(id = R.string.ephemeral_links),
        imageVector = Icons.Filled.Link,
        contentDescription = stringResource(id = R.string.ephemeral_links),
        onValueChanged = onEphemeralLinkPreferenceChanged
    )
}
