package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StandardAppBar(
    title: String,
    navigateBack: () -> Unit = { },
    navigateBackVisible: Boolean = true,
    transparent: Boolean = false,
    actions: @Composable RowScope.() -> Unit = { }
) {
    TopAppBar(
        navigationIcon = {
            if (navigateBackVisible) {
                NavigateBackAppBarAction(
                    onBackClicked = navigateBack
                )
            }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors().copy(
            containerColor = if (transparent)
                Color.Companion.Transparent
            else
                MaterialTheme.colorScheme.background
        ),
        actions = actions
    )
}

@Composable
@Preview
fun StandardAppBarPreview()
{
    StandardAppBar(
        title = "Standard App Bar",
        navigateBack = {},
    )
}
