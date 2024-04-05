package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.R

@Composable
fun ChatInput(
    modifier: Modifier,
    messageInputText: String,
    onInputTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit
) {
    Row (
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ){
        TextField(
            modifier = Modifier.weight(8f),
            value = messageInputText,
            onValueChange = onInputTextChanged,
            shape = RoundedCornerShape(30.dp),
            maxLines = 3,
            colors = TextFieldDefaults.colors().copy(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
        IconButton(
            modifier = Modifier
                .size(64.dp)
                .weight(1f),
            onClick = onSendClicked
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = stringResource(
                    id = R.string.send
                ),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
@Preview
fun ChatInputPreview()
{
    ChatInput(
        modifier = Modifier.fillMaxWidth(),
        messageInputText = "Hello, John",
        onInputTextChanged = {},
        onSendClicked = {}
    )
}
