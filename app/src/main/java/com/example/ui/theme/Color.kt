package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// DocVault design tokens (converted from the web design's oklch palette)
val Paper = Color(0xFFF9F6F0)            // --background
val Ink = Color(0xFF151D23)              // --foreground / --primary
val OnInk = Color(0xFFFFFFFF)            // --primary-foreground
val CardSurface = Color(0xFFFFFFFF)      // --card
val SecondarySurface = Color(0xFFF1EFEA) // --secondary / --muted
val MutedFg = Color(0xFF5A6168)          // --muted-foreground
val SoftIcon = Color(0xFF8A9096)         // low-emphasis icons / captions
val AccentTint = Color(0xFFEDE8FA)       // --accent
val AccentFg = Color(0xFF652EAE)         // --accent-foreground
val ErrorRed = Color(0xFFE7000B)         // --destructive
val BorderColor = Color(0xFFDADFE3)      // --border

// Tone colors
val Violet = Color(0xFF8B5AE4)
val Amber = Color(0xFFE69400)
val Coral = Color(0xFFE1465B)
val Teal = Color(0xFF00917C)
val SkyBlue = Color(0xFF2286EE)

// Still referenced by a few screens
val AccentAmber = Amber

/** Subject colors saved under the old black & white theme (white/grey) would vanish on paper; fall back to violet. */
fun toneOf(hex: Long): Color {
    val c = Color(hex)
    val luminance = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
    return if (luminance > 0.72f) Violet else c
}

// Legacy aliases used by the search screen, mapped onto the new tones
val PdfRed = Coral
val ImagePurple = SkyBlue
val PngTeal = Teal
val WebpAmber = Amber
val GlassCyan = Teal
val GlassViolet = Violet
