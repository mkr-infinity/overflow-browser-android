package com.overflow.browser.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = OverflowPrimary,
    secondary = OverflowSecondary,
    background = BackgroundDark,
    surface = SurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = OverflowPrimary,
    secondary = OverflowSecondary,
    background = BackgroundLight,
    surface = SurfaceLight,
)

@Composable
fun OverflowBrowserTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
