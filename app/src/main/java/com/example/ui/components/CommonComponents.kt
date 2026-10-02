package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentFg
import com.example.ui.theme.AccentTint
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.Ink
import com.example.ui.theme.MutedFg
import com.example.ui.theme.OnInk
import com.example.ui.theme.SecondarySurface
import com.example.ui.theme.Violet
import com.example.ui.theme.toneOf

@Composable
fun FileTypeBadge(
    fileType: String,
    pageCount: Int = 1,
    modifier: Modifier = Modifier
) {
    SpecificFileTypeBadge(
        extension = fileType,
        pageCount = pageCount,
        modifier = modifier
    )
}

@Composable
fun SubjectBadge(
    name: String,
    code: String,
    colorHex: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        color = toneOf(colorHex).copy(alpha = 0.10f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(toneOf(colorHex))
            )
            Spacer(modifier = Modifier.width(6.dp))
            val text = if (code.isNotBlank()) "$code • $name" else name
            Text(
                text = text,
                color = Ink,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModuleBadge(
    moduleNumber: Int,
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SecondarySurface,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MutedFg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Mod $moduleNumber: $name",
                color = MutedFg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LectureMetaChip(
    dateString: String,
    timeString: String = "",
    modifier: Modifier = Modifier
) {
    if (dateString.isBlank() && timeString.isBlank()) return

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (dateString.isNotBlank()) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MutedFg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = dateString,
                fontSize = 11.sp,
                color = MutedFg,
                fontWeight = FontWeight.Normal
            )
        }
        if (timeString.isNotBlank()) {
            if (dateString.isNotBlank()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = MutedFg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = timeString,
                fontSize = 11.sp,
                color = MutedFg,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    actionButtonLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = AccentTint,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Violet,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedFg,
            textAlign = TextAlign.Center
        )
        if (actionButtonLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Ink,
                    contentColor = OnInk
                ),
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = actionButtonLabel,
                    fontWeight = FontWeight.SemiBold,
                    color = OnInk
                )
            }
        }
    }
}

/**
 * Big serif page title with a small muted subtitle (the web design's h1 + count line).
 */
@Composable
fun PageHeading(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 16.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontFamily = DisplayFont,
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 12.sp,
            color = MutedFg
        )
    }
}
