package ro.aenigma.ui.screens.addContacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.ui.screens.common.SimpleInfoScreen

@Composable
fun CodeNotAvailableError(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(id = R.string.qr_code_not_available),
        icon = painterResource(id = R.drawable.ic_qr_code),
        contentDescription = stringResource(id = R.string.qr_code)
    )
}

@Composable
@Preview
fun CodeNotAvailableErrorPreview()
{
    CodeNotAvailableError()
}
