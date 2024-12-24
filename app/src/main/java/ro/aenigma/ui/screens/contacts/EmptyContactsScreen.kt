package ro.aenigma.ui.screens.contacts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.ui.screens.common.SimpleInfoScreen

@Composable
fun EmptyContactsScreen(
    modifier: Modifier = Modifier
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(
            id = R.string.no_contact_available
        ),
        icon = painterResource(
            id = R.drawable.ic_people
        ),
        contentDescription = stringResource(
            id = R.string.no_contact_available
        )
    )
}

@Composable
@Preview
fun EmptyContentPreview()
{
    EmptyContactsScreen()
}