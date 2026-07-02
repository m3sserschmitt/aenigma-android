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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularIndicator(
    visible: Boolean,
    text: String,
    size: Dp = 18.dp,
    color: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = TextStyle.Default
) {
    if (visible) {
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            CircularProgressIndicator(
                strokeWidth = 1.dp,
                modifier = Modifier.size(size),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if(!text.isBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = text,
                    color = textColor,
                    style = textStyle
                )
            }
        }
    }
}

@Preview
@Composable
fun IndeterminateCircularIndicatorPreview()
{
    IndeterminateCircularIndicator(
        visible = true,
        text = "Loading"
    )
}
