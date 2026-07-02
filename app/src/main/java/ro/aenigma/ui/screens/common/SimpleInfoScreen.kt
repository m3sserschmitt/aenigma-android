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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ro.aenigma.util.Constants.Companion.INFO_SCREEN_ICON_SIZE

@Composable
fun SimpleInfoScreen(
    modifier: Modifier = Modifier,
    message: String,
    icon: @Composable () -> Unit
) {
    val cfg = LocalConfiguration.current
    val isPortrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT
    if(isPortrait) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        Row(modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SimpleInfoScreen(
    modifier: Modifier = Modifier,
    message: String,
    icon: Painter,
    contentDescription: String
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = message,
        icon = {
            SimpleInfoScreenIcon(
                icon = icon,
                contentDescription = contentDescription
            )
        }
    )
}

@Composable
fun SimpleInfoScreenIcon(
    icon: Painter,
    contentDescription: String
) {
    Icon(
        modifier = Modifier.size(INFO_SCREEN_ICON_SIZE),
        painter = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onBackground
    )
}
