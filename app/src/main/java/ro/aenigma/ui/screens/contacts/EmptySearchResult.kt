package ro.aenigma.ui.screens.contacts

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
            id = R.string.no_contacts_found
        ),
        icon = painterResource(
            id = R.drawable.ic_people
        ),
        contentDescription = stringResource(
            id = R.string.no_contacts_found
        )
    )
}

@Preview
@Composable
fun EmptySearchResultPreview()
{
    EmptySearchResult()
}