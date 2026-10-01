package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FreshLightScheme = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandSoft,
    onPrimaryContainer = Ink,

    secondary = BrandPink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE3EE),
    onSecondaryContainer = Ink,

    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD5F7F0),
    onTertiaryContainer = Ink,

    background = BgLavender,
    onBackground = Ink,

    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF1EEFF),
    onSurfaceVariant = InkSoft,

    outline = Color(0xFFB9B4DB),
    outlineVariant = Color(0x331E1B4B),

    error = Coral
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // always light & fresh
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FreshLightScheme,
        typography = Typography,
        content = content
    )
}
