package ro.aenigma.ui.screens.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionModeAppBar(
    selectedItemsCount: Int,
    onSelectionModeExited: () -> Unit,
    actions: @Composable () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            CloseAppBarAction(
                onCloseClicked = onSelectionModeExited
            )
        },
        title = {
            Text(
                text = when(selectedItemsCount) {
                    0 -> stringResource(id = R.string.no_item_selected)
                    1 -> stringResource(id = R.string.one_item_selected)
                    else -> stringResource(id = R.string.n_items_selected)
                        .format(selectedItemsCount)
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        actions = {
            actions()
        }
    )
}

@Preview
@Composable
fun SelectionModeAppBarPreview()
{
    SelectionModeAppBar(
        selectedItemsCount = 3,
        onSelectionModeExited = { },
        actions = {}
    )
}
