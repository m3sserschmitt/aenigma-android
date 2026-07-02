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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.QrCodeDto
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.QrCodeGenerator

@Composable
fun LandscapeQrCode(
    qrCode: QrCodeDto
) {
    Image(
        modifier = Modifier
            .padding(
                start = 12.dp,
                end = 12.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .fillMaxHeight(),
        contentScale = ContentScale.FillHeight,
        bitmap = qrCode.code.asImageBitmap(),
        contentDescription = stringResource(
            id = R.string.contact_qr_code
        )
    )
}

@Composable
fun PortraitQrCode(
    qrCode: QrCodeDto
) {
    Image(
        modifier = Modifier
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 6.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .fillMaxWidth(),
        contentScale = ContentScale.FillWidth,
        bitmap = qrCode.code.asImageBitmap(),
        contentDescription = stringResource(
            id = R.string.contact_qr_code
        )
    )
}

@Preview
@Composable
fun QrCodePreview()
{
    val bitmap = QrCodeGenerator(400, 400).encodeAsBitmap("Hello world!")
    if(bitmap != null) {
        ApplicationComposeTheme(darkTheme = true) {
            PortraitQrCode(
                qrCode = QrCodeDto(bitmap, "John", false),
            )
        }
    }
}
