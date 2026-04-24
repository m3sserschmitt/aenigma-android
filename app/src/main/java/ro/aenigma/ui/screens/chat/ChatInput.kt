package ro.aenigma.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.MessageWithDetailsDto
import ro.aenigma.ui.screens.common.FilesCountIndicator
import ro.aenigma.ui.screens.common.FilesPickerButton
import ro.aenigma.ui.screens.common.SendButton
import ro.aenigma.util.RequestState

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    replyToMessage: RequestState<MessageWithDetailsDto>,
    messageInputText: String,
    attachments: List<String>,
    onInputTextChanged: (String) -> Unit,
    onAttachmentsSelected: (List<String>) -> Unit,
    onSendClicked: () -> Unit,
    onReplyAborted: () -> Unit
) {
    if (!visible) {
        return
    }

    Column {
        ReplyToMessage(
            message = replyToMessage,
            onReplyAborted = onReplyAborted
        )
        FilesCountIndicator(
            count = attachments.size,
            onRemoveAttachments = {
                onAttachmentsSelected(listOf())
            }
        )
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilesPickerButton(
                modifier = Modifier.weight(1f),
                onFilesSelected = onAttachmentsSelected
            )
            TextField(
                modifier = Modifier.weight(8f),
                value = messageInputText,
                onValueChange = onInputTextChanged,
                shape = RoundedCornerShape(30.dp),
                maxLines = 3,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = TextFieldDefaults.colors().copy(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
            )
            SendButton(
                modifier = Modifier.size(64.dp).weight(1f),
                tint = MaterialTheme.colorScheme.onBackground,
                onClick = onSendClicked
            )
        }
    }
}

@Composable
fun ReplyToMessage(
    message: RequestState<MessageWithDetailsDto>,
    onReplyAborted: () -> Unit,
) {
    if (message !is RequestState.Success) {
        return
    }
    val name = if (message.data.message.incoming && message.data.sender?.name != null) {
        stringResource(id = R.string.they_said).format(message.data.sender.name)
    } else if(!message.data.message.incoming) {
        stringResource(id = R.string.you_said)
    } else {
        return
    }
    val text = message.data.message.text ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(9f)
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .75f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = onReplyAborted
        ) {
            Icon(
                modifier = Modifier.alpha(.75f),
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(
                    id = R.string.close
                ),
                tint = MaterialTheme.colorScheme.onBackground
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
        visible = true,
        messageInputText = "Hello, John",
        replyToMessage = RequestState.Idle,
        onInputTextChanged = {},
        onSendClicked = {},
        onReplyAborted = {},
        attachments = listOf(),
        onAttachmentsSelected = { },
    )
}
