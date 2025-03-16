package ro.aenigma.ui.screens.chat

import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ro.aenigma.R
import ro.aenigma.ui.screens.common.DialogContentTemplate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveGroupDialog(
    visible: Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit
) {
    if (visible) {
        BasicAlertDialog(onDismissRequest = onDismissClicked) {
            DialogContentTemplate(
                title = stringResource(id = R.string.leave_group),
                body = stringResource(id = R.string.are_you_sure_leave_group),
                dismissible = true,
                onNegativeButtonClicked = onDismissClicked,
                onPositiveButtonClicked = onConfirmClicked,
                content = { }
            )
        }
    }
}

@Composable
@Preview
fun LeaveGroupDialogPreview() {
    LeaveGroupDialog(
        visible = true,
        onDismissClicked = { },
        onConfirmClicked = { }
    )
}
