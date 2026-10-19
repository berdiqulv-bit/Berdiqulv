package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = NavyPrimary,
    primaryContainer = NavyLight,
    onPrimaryContainer = GoldLight,
    secondary = GoldVibrant,
    onSecondary = NavyPrimary,
    secondaryContainer = NavyAccent,
    onSecondaryContainer = GoldLight,
    tertiary = EmeraldKnowledge,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBAC7D5),
    error = CrimsonAlert,
    onError = Color.White,
    outline = Color(0xFF334B68)
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    primaryContainer = NavyLight,
    onPrimaryContainer = GoldLight,
    secondary = GoldDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = GoldDark,
    tertiary = EmeraldKnowledge,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF334B68),
    error = CrimsonAlert,
    onError = Color.White,
    outline = Color(0xFFB0BEC5)
)

@Composable
fun BilimMarkaziProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
