package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OculusDarkColorScheme = darkColorScheme(
    primary = PersonaTeal,
    onPrimary = OculusBackground,
    primaryContainer = OculusSurfaceVariant,
    onPrimaryContainer = PersonaTeal,
    secondary = PersonaNeonPurple,
    onSecondary = OculusBackground,
    secondaryContainer = OculusSurfaceVariant,
    onSecondaryContainer = PersonaNeonPurple,
    tertiary = PersonaAmber,
    onTertiary = OculusBackground,
    background = OculusBackground,
    onBackground = TextPrimary,
    surface = OculusSurface,
    onSurface = TextPrimary,
    surfaceVariant = OculusSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = OculusBorder,
    error = PersonaCrimson,
    onError = TextPrimary
)

@Composable
fun OculusRoundtableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // The Oculus Roundtable enforces a deep dark obsidian aesthetic for immersion
    MaterialTheme(
        colorScheme = OculusDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    OculusRoundtableTheme(content = content)
}
