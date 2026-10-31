package com.overflow.browser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = OverflowPrimary,
    onPrimary = Color.White,
    secondary = OverflowSecondary,
    tertiary = OverflowTertiary,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextPrimaryDark.copy(alpha = 0.8f),
    primaryContainer = OverflowPrimary.copy(alpha = 0.18f),
    onPrimaryContainer = Color.White,
    secondaryContainer = OverflowSecondary.copy(alpha = 0.18f),
    onSecondaryContainer = Color.White,
    outline = OutlineDark,
    error = ErrorAccent,
    onError = Color.White
)

private val LightColors = lightColorScheme(
    primary = OverflowPrimary,
    onPrimary = Color.White,
    secondary = OverflowSecondary,
    tertiary = OverflowTertiary,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextPrimaryLight.copy(alpha = 0.75f),
    primaryContainer = OverflowPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = TextPrimaryLight,
    secondaryContainer = OverflowSecondary.copy(alpha = 0.12f),
    onSecondaryContainer = TextPrimaryLight,
    outline = OutlineLight,
    error = ErrorAccent,
    onError = Color.White
)

@Composable
fun OverflowBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OverflowTypography,
        content = content
    )
}
