package ro.aenigma.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ro.aenigma.util.Constants.Companion.NAVIGATION_BAR_HEIGHT

@Composable
fun BottomSheetTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier.padding(bottom = 4.dp),
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun BottomSheetTemplate(
    navigationBarItems: @Composable (RowScope.() -> Unit) = { },
    content: @Composable (ColumnScope.() -> Unit) = { },
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.background
            ).border(
                width = .25.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp
                )
            ).padding(
                start = 12.dp,
                top = 12.dp,
                end = 12.dp
            )
    ) {
        content()
        NavigationBar(
            modifier = Modifier.fillMaxWidth()
                .height(NAVIGATION_BAR_HEIGHT),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            navigationBarItems()
        }
    }
}
