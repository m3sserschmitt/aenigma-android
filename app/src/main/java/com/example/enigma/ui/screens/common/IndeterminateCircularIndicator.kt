package com.example.enigma.ui.screens.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun IndeterminateCircularIndicator(
    modifier: Modifier = Modifier,
    visible: Boolean,
    text: String,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    if (visible) {
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            CircularProgressIndicator(
                strokeWidth = 1.dp,
                modifier = modifier,
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = text,
                fontSize = fontSize
            )
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