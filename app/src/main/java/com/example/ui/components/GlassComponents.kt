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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.Coral
import com.example.ui.theme.Ink
import com.example.ui.theme.MutedFg
import com.example.ui.theme.OnInk
import com.example.ui.theme.Paper
import com.example.ui.theme.SkyBlue

/**
 * Warm "paper" canvas used behind every screen (name kept so existing screens still compile).
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        content()
    }
}

/**
 * White card with a hairline border (the web design's `bg-card border border-border`).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 12.dp,
    backgroundColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .border(width = borderWidth, color = BorderColor, shape = shape),
        shape = shape,
        color = backgroundColor ?: CardSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        content()
    }
}

/**
 * Pill filter chip. Selected: ink pill with white text. Unselected: white pill with hairline border.
 * [activeColor] tints the icon while unselected (coral for PDF, blue for images, ...).
 */
@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    activeColor: Color = Ink,
    badgeText: String? = null
) {
    val shape = RoundedCornerShape(50)
    val bgColor = if (selected) Ink else CardSurface
    val contentColor = if (selected) OnInk else MutedFg
    val iconColor = if (selected) OnInk else activeColor

    Surface(
        modifier = modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .border(width = 1.dp, color = if (selected) Ink else BorderColor, shape = shape),
        shape = shape,
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            } else if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = OnInk,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )

            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (selected) OnInk else BorderColor,
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Ink else MutedFg
                        )
                    }
                }
            }
        }
    }
}

/**
 * File type badge: coral for PDFs, blue for images (same as the web design's tones).
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
    val tone = if (ext == "PDF") Coral else SkyBlue

    Surface(
        color = tone.copy(alpha = 0.10f),
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tone,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayLabel,
                color = tone,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
