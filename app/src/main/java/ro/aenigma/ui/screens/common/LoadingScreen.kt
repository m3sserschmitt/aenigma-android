package ro.aenigma.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.util.Constants.Companion.INFO_SCREEN_ICON_SIZE

@Composable
fun LoadingScreen(
    modifier: Modifier = Modifier,
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = stringResource(
            id = R.string.loading
        ),
        icon = {
            IndeterminateCircularIndicator(
                visible = true,
                text = "",
                size = INFO_SCREEN_ICON_SIZE,
                color = MaterialTheme.colorScheme.onBackground,
                textStyle = MaterialTheme.typography.bodySmall,
                textColor = MaterialTheme.colorScheme.onBackground
            )
        }
    )
}

@Preview
@Composable
fun LoadingScreenPreview()
{
    LoadingScreen()
}
