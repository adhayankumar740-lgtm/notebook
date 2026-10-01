package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DocumentWithDetails
import com.example.ui.theme.AccentAmber
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.util.FileManager
import java.io.File

@Composable
fun DocumentCard(
    item: DocumentWithDetails,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onEditDetails: () -> Unit,
    onOpenExternal: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val doc = item.document
    var menuExpanded by remember { mutableStateOf(false) }

    val thumbnailFile = doc.thumbnailPath?.let { File(context.filesDir, it) }
    val hasValidThumbnail = thumbnailFile != null && thumbnailFile.exists()
    val specificFormat = DocVaultViewModel.resolveDocumentFormat(doc)

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("document_card_${doc.id}"),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Frosted Thumbnail / Icon Box
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasValidThumbnail) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(thumbnailFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Preview thumbnail for ${doc.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        val icon = if (specificFormat == "PDF") Icons.Default.Description else Icons.Default.Image
                        val tint = if (specificFormat == "PDF") Color(0xFFEF4444) else Color(0xFFA855F7)
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SpecificFileTypeBadge(
                            extension = specificFormat,
                            pageCount = doc.pageCount
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onToggleStar,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("star_button_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = if (doc.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (doc.isStarred) "Starred" else "Not Starred",
                                    tint = if (doc.isStarred) AccentAmber else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box {
                                IconButton(
                                    onClick = { menuExpanded = true },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("menu_button_${doc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Document options",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("View / Read") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Description, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onClick()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Edit Lecture Details") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onEditDetails()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Open in External App") },
                                        leadingIcon = {
                                            Icon(Icons.Default.OpenInNew, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onOpenExternal()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share File") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Share, contentDescription = null)
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onShare()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onDelete()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (doc.topic.isNotBlank()) {
                        Text(
                            text = doc.topic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject and Module badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubjectBadge(
                    name = item.subjectName,
                    code = item.subjectCode,
                    colorHex = item.subjectColorHex
                )
                ModuleBadge(
                    moduleNumber = item.moduleNumber,
                    name = item.moduleName
                )
            }

            // Lecture Date / Time / Lecturer / File Size
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LectureMetaChip(
                    dateString = doc.lectureDateString,
                    timeString = doc.lectureTimeString
                )

                if (doc.lecturerName.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = doc.lecturerName,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Text(
                    text = FileManager.formatFileSize(doc.fileSizeBytes),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }

            // Tags row (custom tags with glass badges)
            if (doc.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val tagList = doc.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(3)
                    for (tag in tagList) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
