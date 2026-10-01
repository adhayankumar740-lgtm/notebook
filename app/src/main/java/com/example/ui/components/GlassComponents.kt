package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgLavender
import com.example.ui.theme.BgMint
import com.example.ui.theme.BgPeach
import com.example.ui.theme.BgSky
import com.example.ui.theme.Brand
import com.example.ui.theme.BrandPink
import com.example.ui.theme.Coral
import com.example.ui.theme.GlassStroke
import com.example.ui.theme.GlassStrokeSoft
import com.example.ui.theme.GlassWhite
import com.example.ui.theme.ImagePurple
import com.example.ui.theme.InkSoft
import com.example.ui.theme.Mint
import com.example.ui.theme.Sky
import com.example.ui.theme.Sun

/**
 * Light pastel canvas with slowly floating colourful blobs.
 * Cards on top are semi-transparent, so the colours softly shine through (glass effect).
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "bg")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgShift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BgLavender, BgPeach, BgSky)
                )
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                val s = shift

                fun blob(color: Color, cx: Float, cy: Float, r: Float) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color, Color.Transparent),
                            center = Offset(cx, cy),
                            radius = r
                        ),
                        radius = r,
                        center = Offset(cx, cy)
                    )
                }

                blob(BrandPink.copy(alpha = 0.28f), w * (0.85f - 0.10f * s), h * (0.10f + 0.05f * s), w * 0.65f)
                blob(Brand.copy(alpha = 0.22f), w * (0.05f + 0.12f * s), h * (0.40f - 0.05f * s), w * 0.70f)
                blob(Mint.copy(alpha = 0.22f), w * (0.90f - 0.15f * s), h * (0.70f + 0.04f * s), w * 0.65f)
                blob(Sun.copy(alpha = 0.20f), w * (0.20f + 0.10f * s), h * (0.95f - 0.05f * s), w * 0.60f)
                blob(Sky.copy(alpha = 0.18f), w * 0.5f, h * (0.55f + 0.06f * s), w * 0.55f)
            }
    ) {
        content()
    }
}

/**
 * Frosted glass card: semi-transparent white, bright rim, soft coloured shadow,
 * and a small "press" bounce so it feels alive when tapped.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    backgroundColor: Color? = null,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val resolvedBg = backgroundColor ?: GlassWhite
    val shape = RoundedCornerShape(cornerRadius)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "cardPress"
    )

    val borderBrush = Brush.linearGradient(
        listOf(GlassStroke, GlassStrokeSoft, Color(0x33FFFFFF), GlassStroke)
    )

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = Brand.copy(alpha = 0.18f),
                spotColor = Brand.copy(alpha = 0.28f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = LocalIndication.current,
                        onClick = onClick
                    )
                } else Modifier
            )
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        shape = shape,
        color = resolvedBg
    ) {
        content()
    }
}

/**
 * Colourful glass filter chip.
 * Selected: solid activeColor pill with white text. Unselected: translucent white glass.
 */
@Composable
fun GlassFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    activeColor: Color = Brand,
    badgeText: String? = null
) {
    val shape = RoundedCornerShape(14.dp)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipPress"
    )

    val bgColor = if (selected) activeColor else Color(0x99FFFFFF)
    val contentColor = if (selected) Color.White else InkSoft
    val borderColor = if (selected) activeColor else Color(0xCCFFFFFF)

    Surface(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .clickable(
                interactionSource = interaction,
                indication = LocalIndication.current,
                onClick = onClick
            )
            .border(width = 1.dp, color = borderColor, shape = shape),
        shape = shape,
        color = bgColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else activeColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
            } else if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            )

            if (!badgeText.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (selected) Color.White else activeColor.copy(alpha = 0.18f),
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) activeColor else InkSoft
                        )
                    }
                }
            }
        }
    }
}

/**
 * Colour coded file badge: PDF = coral, JPG = violet, PNG = teal, WEBP = amber.
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

    val accent = when (ext) {
        "PDF" -> Coral
        "JPG", "JPEG" -> ImagePurple
        "PNG" -> Mint
        "WEBP" -> Sun
        else -> Brand
    }

    Surface(
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.border(0.8.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = displayLabel,
                color = accent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.4.sp
            )
        }
    }
}
