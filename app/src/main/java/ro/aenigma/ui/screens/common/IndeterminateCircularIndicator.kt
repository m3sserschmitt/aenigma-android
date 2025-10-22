package ro.aenigma.ui.screens.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularIndicator(
    visible: Boolean,
    text: String,
    size: Dp = 18.dp,
    color: Color = Color.Unspecified,
    textColor: Color = Color.Unspecified,
    textStyle: TextStyle = TextStyle.Default
) {
    if (visible) {
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            CircularProgressIndicator(
                strokeWidth = 1.dp,
                modifier = Modifier.size(size),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            if(!text.isBlank()) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = text,
                    color = textColor,
                    style = textStyle
                )
            }
        }
    }
}

@Preview
@Composable
fun IndeterminateCircularIndicatorPreview()
{
    IndeterminateCircularIndicator(
        visible = true,
        text = "Loading"
    )
}
