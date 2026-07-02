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
import ro.aenigma.R

@Composable
fun InstallOrbotDialog(
    visible: Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit,
) {
    if(visible) {
        QuestionDialog(
            title = stringResource(id = R.string.orbot_required_title),
            question = stringResource(id = R.string.do_you_install_orbot),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onDismissClicked
        )
    }
}

@Composable
fun OrbotInfoDialog(
    visible: Boolean,
    onLaunchOrbot: () -> Unit = { },
    onConfirmClicked: () -> Unit = { },
) {
    if (visible) {
        QuestionDialog(
            title = stringResource(id = R.string.use_orbot_title),
            question = stringResource(id = R.string.orbot_info),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onLaunchOrbot,
            negativeButtonText = stringResource(id = R.string.open_orbot),
        )
    }
}

@Composable
fun TorInfoDialog(
    visible: Boolean,
    onLaunchOrbot: () -> Unit = { },
    onConfirmClicked: () -> Unit = { }
) {
    if(visible) {
        QuestionDialog(
            title = stringResource(id = R.string.start_tor_service_title),
            question = stringResource(id = R.string.tor_service_info),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onLaunchOrbot,
            negativeButtonText = stringResource(id = R.string.open_orbot)
        )
    }
}
