package ro.aenigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    text: String
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = text,
        icon = painterResource(
            id = R.drawable.ic_error
        ),
        contentDescription = stringResource(
            id = R.string.something_went_wrong
        )
    )
}

@Composable
fun GenericErrorScreen(
    modifier: Modifier = Modifier
) {
    ErrorScreen(
        modifier = modifier,
        text = stringResource(
            id = R.string.something_went_wrong
        )
    )
}

@Preview
@Composable
fun ErrorScreenPreview()
{
    GenericErrorScreen()
}
