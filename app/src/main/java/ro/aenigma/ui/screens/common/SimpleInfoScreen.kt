package ro.aenigma.ui.screens.common

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SimpleInfoScreen(
    modifier: Modifier = Modifier,
    message: String,
    icon: @Composable () -> Unit
) {
    val cfg = LocalConfiguration.current
    val isPortrait = cfg.orientation == Configuration.ORIENTATION_PORTRAIT
    if(isPortrait) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        Row(modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            icon()
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun SimpleInfoScreen(
    modifier: Modifier = Modifier,
    message: String,
    icon: Painter,
    contentDescription: String
) {
    SimpleInfoScreen(
        modifier = modifier,
        message = message,
        icon = {
            SimpleInfoScreenIcon(
                icon = icon,
                contentDescription = contentDescription
            )
        }
    )
}

@Composable
fun SimpleInfoScreenIcon(
    icon: Painter,
    contentDescription: String
) {
    Icon(
        modifier = Modifier.size(60.dp),
        painter = icon,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onBackground
    )
}
