package ro.aenigma.ui.screens.addContacts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.QrCodeDto
import ro.aenigma.ui.themes.ApplicationComposeTheme
import ro.aenigma.util.QrCodeGenerator

@Composable
fun QrCode(
    modifier: Modifier = Modifier,
    qrCode: QrCodeDto
) {
    Column(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.background
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            modifier = Modifier
                .padding(horizontal = 28.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyLarge,
            text = stringResource(
                id = R.string.qr_code_caption
            )
        )
        Image(
            modifier = Modifier
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = 12.dp
                )
                .clip(RoundedCornerShape(24.dp))
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            bitmap = qrCode.code.asImageBitmap(),
            contentDescription = stringResource(
                id = R.string.contact_qr_code
            )
        )
        Text(
            modifier = Modifier.alpha(.75f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            text = qrCode.label,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun QrCodePreview()
{
    val bitmap = QrCodeGenerator(400, 400).encodeAsBitmap("Hello world!")
    if(bitmap != null) {
        ApplicationComposeTheme(darkTheme = true) {
            QrCode(
                qrCode = QrCodeDto(bitmap, "John", false),
            )
        }
    }
}
