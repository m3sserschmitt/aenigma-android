package ro.aenigma.ui.themes

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

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
fun ApplicationMaterialTheme(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun ApplicationComposeDarkTheme(content: @Composable () -> Unit) {
    ApplicationMaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}

@Composable
fun ApplicationComposeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    val bg = colorScheme.background
    SideEffect {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val lightIcons = bg.luminance() > 0.5f
        controller.isAppearanceLightStatusBars = lightIcons
        controller.isAppearanceLightNavigationBars = lightIcons
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(bg)
    ) {
        Box(
            modifier = Modifier.statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            ApplicationMaterialTheme(
                colorScheme = colorScheme,
                content = content
            )
        }
    }
}
