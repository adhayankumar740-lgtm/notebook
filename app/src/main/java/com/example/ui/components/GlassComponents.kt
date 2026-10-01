package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.GlassBorderHighlight
import com.example.ui.theme.GlassBorderSubtle
import com.example.ui.theme.GlassDarkCard
import com.example.ui.theme.GlassSmokedBlack
import com.example.ui.theme.Platinum
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.Silver

/**
 * Modern sleek Black & White ambient background.
 * Deep obsidian canvas with soft monochrome silver & white diffuse glows that shine
 * through frosted glass cards.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureBlack)
            .drawBehind {
                val width = size.width
                val height = size.height

                // Subtle top-right white specular glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.85f, height * 0.12f),
                        radius = width * 0.70f
                    )
                )

                // Mid-left silver diffused glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE5E5EA).copy(alpha = 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.10f, height * 0.45f),
                        radius = width * 0.65f
                    )
                )

                // Bottom soft white glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        center = Offset(width * 0.50f, height * 0.88f),
                        radius = width * 0.75f
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Modern Sleek Smoked Glass Card with luminous white specular edge borders.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    backgroundColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val resolvedBg = backgroundColor ?: GlassDarkCard
    val shape = RoundedCornerShape(cornerRadius)

    val borderBrush = Brush.linearGradient(
        listOf(
            GlassBorderHighlight,
            GlassBorderSubtle,
            Color(0x05FFFFFF),
            GlassBorderSubtle
        )
    )

    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        shape = shape,
        color = resolvedBg,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        content()
    }
}

/**
 * Sleek Black & White Glass Filter Chip.
 * Selected: Pure White pill with Black text/icon (maximum contrast).
 * Unselected: Smoked translucent glass with silver text and fine white outline.
 */
@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    activeColor: Color = PureWhite,
    badgeText: String? = null
) {
    val shape = RoundedCornerShape(12.dp)

    val bgColor = if (selected) {
        PureWhite
    } else {
        Color(0x261C1C1E)
    }

    val contentColor = if (selected) {
        PureBlack
    } else {
        Silver
    }

    val borderBrush = if (selected) {
        Brush.linearGradient(
            listOf(
                Color.White,
                Color(0xCCFFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x29FFFFFF),
                Color(0x0DFFFFFF)
            )
        )
    }

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = shape
            ),
        shape = shape,
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            } else if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = PureBlack,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )

            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (selected) PureBlack else Color(0x33FFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) PureWhite else Silver
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern sleek Monochrome file type badge (Pure Black & White with subtle frosted tint).
 */
@Composable
fun SpecificFileTypeBadge(
    extension: String,
    pageCount: Int = 1,
    modifier: Modifier = Modifier
) {
    val ext = extension.uppercase().trim().replace(".", "")
    val displayLabel = when {
        ext == "PDF" -> if (pageCount > 1) "PDF • ${pageCount}p" else "PDF"
        ext == "JPG" || ext == "JPEG" -> "JPG"
        ext == "PNG" -> "PNG"
        ext == "WEBP" -> "WEBP"
        else -> ext.ifBlank { "FILE" }
    }

    val icon = when (ext) {
        "PDF" -> Icons.Default.Description
        "JPG", "JPEG" -> Icons.Default.PhotoLibrary
        else -> Icons.Default.Image
    }

    Surface(
        color = Color(0x26FFFFFF),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier.border(
            0.5.dp,
            Color(0x40FFFFFF),
            RoundedCornerShape(6.dp)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PureWhite,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayLabel,
                color = PureWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
