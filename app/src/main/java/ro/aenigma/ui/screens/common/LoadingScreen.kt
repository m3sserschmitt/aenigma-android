package ro.aenigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

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
                text = ""
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