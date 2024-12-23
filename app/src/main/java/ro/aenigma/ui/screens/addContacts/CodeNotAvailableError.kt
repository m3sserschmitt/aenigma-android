package ro.aenigma.ui.screens.addContacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R

@Composable
fun CodeNotAvailableError()
{
    Column (
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_qr_code),
            modifier = Modifier.size(120.dp),
            contentDescription = stringResource(id = R.string.qr_code)
        )
        Text(
            fontWeight = FontWeight.Bold,
            text = stringResource(id = R.string.qr_code_not_available)
        )
    }
}

@Composable
@Preview
fun CodeNotAvailableErrorPreview()
{
    CodeNotAvailableError()
}
