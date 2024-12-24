package ro.aenigma.ui.screens.chat

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.ui.screens.common.SimpleInfoScreen

@Composable
fun EmptySearchResult(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(
            id = R.string.no_message_found
        ),
        icon = painterResource(
            id = R.drawable.ic_message
        ),
        contentDescription = stringResource(
            id = R.string.no_message_found
        )
    )
}

@Preview
@Composable
fun EmptySearchResultPreview()
{
    EmptySearchResult()
}
