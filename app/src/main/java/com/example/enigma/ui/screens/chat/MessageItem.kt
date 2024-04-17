package com.example.enigma.ui.screens.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.enigma.R
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.ui.screens.common.selectable
import com.example.enigma.util.prettyDateFormatting
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun MessageItem(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onItemSelected: (MessageEntity) -> Unit,
    onItemDeselected: (MessageEntity) -> Unit,
    onClick: () -> Unit,
    message: MessageEntity
) {
    val paddingStart = if(message.incoming) 8.dp else 50.dp
    val paddingEnd = if(message.incoming) 50.dp else 8.dp

    Box (
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingStart, 8.dp, paddingEnd, 0.dp),
        contentAlignment = if(message.incoming)
            Alignment.CenterStart
        else
            Alignment.CenterEnd
    ) {
        Surface (
            modifier = Modifier
                .selectable(
                    item = message,
                    isSelectionMode = isSelectionMode,
                    isSelected = isSelected,
                    onItemSelected = onItemSelected,
                    onItemDeselected = onItemDeselected,
                    onClick = onClick
                ),
            color = if(message.incoming)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(isSelectionMode)
                {
                    if(isSelected)
                    {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                    else
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_radio_button_unchecked),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(IntrinsicSize.Max)
                ) {

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = message.text,
                        textAlign = if (message.incoming)
                            TextAlign.Start
                        else
                            TextAlign.End
                    )

                    MessageLiveDate(
                        message = message
                    )
                }
            }
        }
    }
}

@Composable
fun MessageLiveDate(
    message: MessageEntity
) {
    var textDate by remember {
        mutableStateOf(prettyDateFormatting(message.date))
    }

    LaunchedEffect(key1 = true) {
        while (true) {
            delay(1000 * 60)
            textDate = prettyDateFormatting(message.date)
        }
    }

    Text(
        modifier = Modifier
            .alpha(0.5f)
            .fillMaxWidth(),
        textAlign = TextAlign.End,
        text = textDate
    )
}

@Composable
@Preview
fun MessageItemPreview()
{
    MessageItem(
        isSelectionMode = true,
        isSelected = false,
        message = MessageEntity(
            "123-123-123-123",
            "Hello",
            true,
            Date()
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}

@Composable
@Preview
fun MessageItemSelectedPreview()
{
    MessageItem(
        isSelectionMode = true,
        isSelected = true,
        message = MessageEntity(
            "123-123-123-123",
            "Hello",
            true,
            Date()
        ),
        onItemDeselected = {},
        onClick = {},
        onItemSelected = {}
    )
}
