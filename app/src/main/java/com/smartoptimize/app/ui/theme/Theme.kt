package com.smartoptimize.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealContainer,
    onPrimaryContainer = Color(0xFF04211D),
    secondary = Color(0xFF0E7490),
    onSecondary = Color.White,
    background = SlateBackground,
    onBackground = SlateOnSurface,
    surface = SlateSurface,
    onSurface = SlateOnSurface,
    surfaceVariant = Color(0xFFE7EEF5),
    onSurfaceVariant = Color(0xFF475569),
    error = RedCritical,
    errorContainer = RedCriticalContainer
)

private val DarkColors = darkColorScheme(
    primary = TealPrimaryDark,
    onPrimary = Color(0xFF04211D),
    primaryContainer = TealContainerDark,
    onPrimaryContainer = TealContainer,
    secondary = Color(0xFF67E8F9),
    onSecondary = Color(0xFF083344),
    background = SlateBackgroundDark,
    onBackground = SlateOnSurfaceDark,
    surface = SlateSurfaceDark,
    onSurface = SlateOnSurfaceDark,
    surfaceVariant = Color(0xFF24314D),
    onSurfaceVariant = Color(0xFFA8B7CC),
    error = Color(0xFFFCA5A5),
    errorContainer = Color(0xFF450A0A)
)

@Composable
fun SmartOptimizeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
