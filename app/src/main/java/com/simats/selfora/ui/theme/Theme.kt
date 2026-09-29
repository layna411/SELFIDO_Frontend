package com.simats.selfora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SelforaPrimary,
    onPrimary = SelforaSurface,
    primaryContainer = SelforaBackground,
    onPrimaryContainer = SelforaTextPrimary,
    secondary = SelforaSecondary,
    onSecondary = SelforaSurface,
    tertiary = SelforaSuccess,
    onTertiary = SelforaSurface,
    background = SelforaBackground,
    surface = SelforaSurface,
    onBackground = SelforaTextPrimary,
    onSurface = SelforaTextPrimary,
    outline = SelforaBorder,
    error = SelforaError,
    onError = SelforaSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = SelforaDarkPrimary,
    onPrimary = SelforaDarkBackground,
    primaryContainer = SelforaDarkSurface,
    onPrimaryContainer = SelforaDarkTextPrimary,
    secondary = SelforaDarkSecondary,
    onSecondary = SelforaDarkBackground,
    tertiary = SelforaDarkSuccess,
    onTertiary = SelforaDarkBackground,
    background = SelforaDarkBackground,
    surface = SelforaDarkSurface,
    onBackground = SelforaDarkTextPrimary,
    onSurface = SelforaDarkTextPrimary,
    outline = SelforaBorder,
    error = SelforaError,
    onError = SelforaSurface
)

// Standard UI Shapes per Design System
val CardCornerShape = RoundedCornerShape(18.dp)
val ButtonCornerShape = RoundedCornerShape(12.dp)
val ChildCardCornerShape = RoundedCornerShape(20.dp)
val ChildButtonCornerShape = RoundedCornerShape(24.dp)

@Composable
fun SELFORATheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}