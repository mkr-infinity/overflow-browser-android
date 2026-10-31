package com.overflow.browser.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Overflow brand palette: deep teal, warm coral, and a calm blue.
val OverflowTeal = Color(0xFF006A6A)
val OverflowTealLight = Color(0xFF80D5D3)
val OverflowCoral = Color(0xFF9C4236)
val OverflowCoralLight = Color(0xFFFFB4A8)
val OverflowBlue = Color(0xFF3F5F90)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF9CF1EF),
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFF9C4236),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD4),
    onSecondaryContainer = Color(0xFF410001),
    tertiary = Color(0xFF3F5F90),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD6E3FF),
    onTertiaryContainer = Color(0xFF001B3D),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF5FAFA),
    onBackground = Color(0xFF161D1D),
    surface = Color(0xFFF5FAFA),
    onSurface = Color(0xFF161D1D),
    surfaceVariant = Color(0xFFDAE5E4),
    onSurfaceVariant = Color(0xFF3F4948),
    outline = Color(0xFF6F7979),
    outlineVariant = Color(0xFFBEC9C8),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFF2B3231),
    inverseOnSurface = Color(0xFFECF2F1),
    inversePrimary = Color(0xFF80D5D3),
    surfaceTint = Color(0xFF006A6A)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF80D5D3),
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFF9CF1EF),
    secondary = Color(0xFFFFB4A8),
    onSecondary = Color(0xFF5F150D),
    secondaryContainer = Color(0xFF7F2A20),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFFA8C8FF),
    onTertiary = Color(0xFF082F5F),
    tertiaryContainer = Color(0xFF264777),
    onTertiaryContainer = Color(0xFFD6E3FF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0E1515),
    onBackground = Color(0xFFDDE4E3),
    surface = Color(0xFF0E1515),
    onSurface = Color(0xFFDDE4E3),
    surfaceVariant = Color(0xFF3F4948),
    onSurfaceVariant = Color(0xFFBEC9C8),
    outline = Color(0xFF899393),
    outlineVariant = Color(0xFF3F4948),
    scrim = Color(0xFF000000),
    inverseSurface = Color(0xFFDDE4E3),
    inverseOnSurface = Color(0xFF2B3231),
    inversePrimary = Color(0xFF006A6A),
    surfaceTint = Color(0xFF80D5D3)
)

val BlackColorScheme = DarkColorScheme.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceVariant = Color(0xFF151B1B),
    onBackground = Color(0xFFE6EAE9),
    onSurface = Color(0xFFE6EAE9)
)
