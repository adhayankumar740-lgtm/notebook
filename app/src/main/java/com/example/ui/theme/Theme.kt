package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// High-contrast Sleek Black & White Monochrome Palette
private val BlackAndWhiteScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = Color(0xFF242426),
    onPrimaryContainer = PureWhite,

    secondary = Platinum,
    onSecondary = PureBlack,
    secondaryContainer = Color(0xFF1C1C1E),
    onSecondaryContainer = PureWhite,

    tertiary = Silver,
    onTertiary = PureBlack,

    background = PureBlack,
    onBackground = PureWhite,

    surface = Obsidian,
    onSurface = PureWhite,

    surfaceVariant = DarkCharcoal,
    onSurfaceVariant = Silver,

    outline = CharcoalBorder,
    outlineVariant = Color(0x33FFFFFF)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek black glass
    dynamicColor: Boolean = false, // Keep pure black & white design
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BlackAndWhiteScheme,
        typography = Typography,
        content = content
    )
}
