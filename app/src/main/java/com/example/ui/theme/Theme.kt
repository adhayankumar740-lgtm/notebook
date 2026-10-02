package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DocVaultScheme = lightColorScheme(
    primary = Ink,
    onPrimary = OnInk,
    primaryContainer = AccentTint,
    onPrimaryContainer = AccentFg,

    secondary = Violet,
    onSecondary = OnInk,
    secondaryContainer = SecondarySurface,
    onSecondaryContainer = Ink,

    tertiary = Violet,
    onTertiary = OnInk,

    background = Paper,
    onBackground = Ink,

    surface = CardSurface,
    onSurface = Ink,
    surfaceVariant = SecondarySurface,
    onSurfaceVariant = MutedFg,
    surfaceTint = CardSurface,

    surfaceContainerLowest = CardSurface,
    surfaceContainerLow = CardSurface,
    surfaceContainer = CardSurface,
    surfaceContainerHigh = CardSurface,
    surfaceContainerHighest = SecondarySurface,

    outline = SoftIcon,
    outlineVariant = BorderColor,

    error = ErrorRed,
    onError = OnInk
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DocVaultScheme,
        typography = Typography,
        content = content
    )
}
