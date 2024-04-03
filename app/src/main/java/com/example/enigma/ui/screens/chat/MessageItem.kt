package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.data.database.MessageEntity
import java.util.Date

@Composable
fun MessageItem(
    message: MessageEntity
)
{
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 8.dp, 0.dp),
        contentAlignment = if(message.incoming)
            Alignment.CenterStart
        else
            Alignment.CenterEnd
    ) {
        Surface (
            color = if(message.incoming)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier= Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.text
                )
                Text(
                    modifier = Modifier.alpha(0.5f),
                    text = message.date.toString()
                )
            }
        }
    }
}

@Composable
@Preview
fun MessageItemPreview()
{
    MessageItem(
        message = MessageEntity(
            "123-123-123-123",
            "Hello",
            true,
            Date()
        )
    )
}
