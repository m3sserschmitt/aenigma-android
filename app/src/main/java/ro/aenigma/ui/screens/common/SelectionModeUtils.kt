package ro.aenigma.ui.screens.common

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ro.aenigma.R

@Composable
fun ExitSelectionMode(
    isSelectionMode: Boolean,
    selectedItemsCount: Int,
    onSelectionModeExited: () -> Unit
) {
    LaunchedEffect(key1 = isSelectionMode, key2 = selectedItemsCount)
    {
        if(isSelectionMode && selectedItemsCount == 0)
        {
            onSelectionModeExited()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun <T> Modifier.selectable(
    item: T,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (T) -> Unit,
    onItemDeselected: (T) -> Unit,
    onClick: (T) -> Unit
): Modifier {
    return this.combinedClickable(
        onClick = {
            if(isSelectionMode && !isSelected){
                onItemSelected(item)
            }
            else if(isSelectionMode)
            {
                onItemDeselected(item)
            } else {
                onClick(item)
            }
        },
        onLongClick = {
            if(!isSelected)
            {
                onItemSelected(item)
            }
        }
    )
}

@Composable
fun SelectionModeBullet(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    contentColor: Color = Color.Unspecified
) {
    if (isSelectionMode) {
        if (isSelected) {
            Icon(
                modifier = Modifier.alpha(.5f),
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = stringResource(R.string.message_selection),
                tint = contentColor,
            )
        } else {
            Icon(
                modifier = Modifier.alpha(.5f),
                painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                contentDescription = stringResource(R.string.message_selection),
                tint = contentColor,
            )
        }
    }
}
