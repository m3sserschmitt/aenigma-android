package ro.aenigma.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import ro.aenigma.R

@Composable
fun BasicDropdownMenu(
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    actions: @Composable ColumnScope.() -> Unit
) {
    Box{
        IconButton(
            onClick = {
                onToggle(!expanded)
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(
                    id = R.string.more_actions
                ),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        DropdownMenu(
            modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
            expanded = expanded,
            onDismissRequest = {
                onToggle(false)
            },
            content = actions
        )
    }
}

@Composable
fun BasicDropDownMenuItem(
    imageVector: ImageVector,
    contentDescription: String,
    text: String,
    enabled: Boolean = true,
    visible: Boolean = true,
    onClick: () -> Unit
) {
    if (visible) {
        DropdownMenuItem(
            enabled = enabled,
            leadingIcon = {
                Icon(
                    modifier = Modifier.alpha(.75f),
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            text = {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            onClick = onClick
        )
    }
}
