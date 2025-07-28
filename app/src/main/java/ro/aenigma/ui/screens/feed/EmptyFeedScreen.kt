package ro.aenigma.ui.screens.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ro.aenigma.R
import ro.aenigma.ui.screens.common.SimpleInfoScreen

@Composable
fun EmptyFeedScreen() {
    SimpleInfoScreen(
        message = stringResource(
            id = R.string.there_is_nothing_here
        ),
        icon = painterResource(
            id = R.drawable.ic_article
        ),
        contentDescription = stringResource(
            id = R.string.there_is_nothing_here
        )
    )
}
