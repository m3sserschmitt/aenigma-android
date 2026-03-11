package ro.aenigma.ui.screens.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ro.aenigma.R

@Composable
fun InstallOrbotDialog(
    visible: Boolean,
    onConfirmClicked: () -> Unit,
    onDismissClicked: () -> Unit,
) {
    if(visible) {
        QuestionDialog(
            title = stringResource(id = R.string.orbot_required_title),
            question = stringResource(id = R.string.do_you_install_orbot),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onDismissClicked
        )
    }
}

@Composable
fun OrbotInfoDialog(
    visible: Boolean,
    onLaunchOrbot: () -> Unit = { },
    onConfirmClicked: () -> Unit = { },
) {
    if (visible) {
        QuestionDialog(
            title = stringResource(id = R.string.use_orbot_title),
            question = stringResource(id = R.string.orbot_info),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onLaunchOrbot,
            negativeButtonText = stringResource(id = R.string.open_orbot),
        )
    }
}

@Composable
fun TorInfoDialog(
    visible: Boolean,
    onLaunchOrbot: () -> Unit = { },
    onConfirmClicked: () -> Unit = { }
) {
    if(visible) {
        QuestionDialog(
            title = stringResource(id = R.string.start_tor_service_title),
            question = stringResource(id = R.string.tor_service_info),
            onPositiveButtonClicked = onConfirmClicked,
            onNegativeButtonClicked = onLaunchOrbot,
            negativeButtonText = stringResource(id = R.string.open_orbot)
        )
    }
}
