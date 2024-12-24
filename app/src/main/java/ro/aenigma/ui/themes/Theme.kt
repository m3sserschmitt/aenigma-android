package ro.aenigma.ui.themes

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF9575CD), // Light Purple
    onPrimary = Color(0xFFFFFFFF), // White Text on Primary
    primaryContainer = Color(0xFFD1C4E9), // Soft Purple Container
    onPrimaryContainer = Color(0xFF512DA8), // Dark Purple Text on Container
    secondary = Color(0xFFB39DDB), // Muted Purple
    onSecondary = Color(0xFF311B92), // Deep Purple Text
    secondaryContainer = Color(0xFFEDE7F6), // Very Light Purple for Contrast
    onSecondaryContainer = Color(0xFF311B92), // Deep Purple Text on Secondary Container
    background = Color(0xFFF5F5F5), // Light Gray Background
    onBackground = Color(0xFF424242), // Dark Gray Text on Background
    surface = Color(0xFFE8EAF6), // Light Purple-Gray Surface
    onSurface = Color(0xFF1A237E), // Dark Blue Text on Surface
    surfaceContainerHighest = Color(0xFFD1C4E9) // Slightly Darker Purple for Elevation
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF757575), // Medium Gray
    onPrimary = Color(0xFFFAFAFA), // Light Text
    primaryContainer = Color(0xFF424242), // Dark Container
    onPrimaryContainer = Color(0xFFECEFF1), // Light Text on Dark Container
    secondary = Color(0xFF616161), // Secondary Gray
    onSecondary = Color(0xFFE0E0E0), // Light Text on Secondary
    secondaryContainer = Color(0xA0151517), // Dark Gray for Contrast
    onSecondaryContainer = Color(0xFFCFD8DC), // Lighter Text on Secondary Container
    background = Color(0xFF212121), // Almost Black Gray
    onBackground = Color(0xFFBDBDBD), // Text on Dark Background
    surface = Color(0xFF303030), // Dark Surface
    onSurface = Color(0xFFE0E0E0), // Light Text on Surface
    surfaceContainerHighest = Color(0xFF3A3A3A) // Lighter Surface for Elevation
)

@Composable
fun ApplicationComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val activity = LocalContext.current as Activity
    activity.window?.let { window ->
        window.statusBarColor = colors.background.toArgb()
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
