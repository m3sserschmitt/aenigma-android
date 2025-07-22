package ro.aenigma.ui.screens.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ro.aenigma.R
import ro.aenigma.models.MessageWithDetailsDto

@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    replyToMessage: MessageWithDetailsDto?,
    messageInputText: String,
    attachments: List<String>,
    onInputTextChanged: (String) -> Unit,
    onAttachmentsSelected: (List<String>) -> Unit,
    onSendClicked: () -> Unit,
    onReplyAborted: () -> Unit
) {
    val context = LocalContext.current
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        val contentResolver = context.contentResolver
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        uris.forEach { uri ->
            try {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (_: SecurityException) {
            }
        }

        onAttachmentsSelected(uris.map { it.toString() })
    }

    Column {
        ReplyToMessage(
            message = replyToMessage,
            onReplyAborted = onReplyAborted
        )
        SelectedAttachments(
            files = attachments,
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
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    multiplePhotoPicker.launch(arrayOf("*/*"))
                }
            ) {
                Icon(
                    modifier = Modifier.alpha(.75f),
                    painter = painterResource(id = R.drawable.ic_attachement),
                    contentDescription = stringResource(
                        id = R.string.send_file
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
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
                enabled = enabled
            )
            IconButton(
                modifier = Modifier
                    .size(64.dp)
                    .weight(1f),
                onClick = onSendClicked,
                enabled = enabled
            ) {
                Icon(
                    modifier = Modifier.alpha(.75f),
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(
                        id = R.string.send
                    ),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun SelectedAttachments(
    files: List<String>,
    onRemoveAttachments: () -> Unit
) {
    if(files.isNotEmpty())
    {
        Row {
            Text(
                modifier = Modifier
                    .weight(9f)
                    .padding(4.dp)
                    .align(alignment = Alignment.CenterVertically),
                text = if (files.size > 1)
                    stringResource(id = R.string.n_attachments_selected).format(files.size)
                else
                    stringResource(id = R.string.one_attachment_selected),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                modifier = Modifier.weight(1f),
                onClick = onRemoveAttachments
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
}

@Composable
fun ReplyToMessage(
    message: MessageWithDetailsDto?,
    onReplyAborted: () -> Unit,
) {
    if(message == null){
        return
    }
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
                text = if (message.message.incoming && message.sender != null)
                    stringResource(id = R.string.they_said).format(message.sender.name)
                else
                    stringResource(id = R.string.you_said),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                modifier = Modifier.padding(start = 4.dp),
                text = message.message.text.toString(),
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
        enabled = true,
        messageInputText = "Hello, John",
        replyToMessage = null,
        onInputTextChanged = {},
        onSendClicked = {},
        onReplyAborted = {},
        attachments = listOf(),
        onAttachmentsSelected = { },
    )
}
