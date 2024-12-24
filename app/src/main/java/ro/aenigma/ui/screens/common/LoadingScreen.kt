package ro.aenigma.ui.screens.common

import androidx.compose.material3.MaterialTheme
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
                text = "",
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
