package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DocumentWithDetails
import com.example.ui.components.FileTypeBadge
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.LectureMetaChip
import com.example.ui.components.ModuleBadge
import com.example.ui.components.SpecificFileTypeBadge
import com.example.ui.components.SubjectBadge
import com.example.ui.theme.AccentAmber
import com.example.util.FileManager
import com.example.util.PdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    item: DocumentWithDetails,
    onClose: () -> Unit,
    onToggleStar: (Long, Boolean) -> Unit,
    onEditDetails: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }
    val context = LocalContext.current
    val doc = item.document
    val file = remember(doc.internalFilePath) { FileManager.getInternalFile(context, doc.internalFilePath) }

    var selectedViewerTab by remember { mutableIntStateOf(0) } // 0: View File, 1: Lecture Details
    var currentPageIndex by remember { mutableIntStateOf(0) }
    var currentPdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoadingPage by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val isPdf = doc.fileType.equals("PDF", ignoreCase = true)

    // Render PDF page when page index changes
    LaunchedEffect(file, currentPageIndex, isPdf) {
        if (isPdf && file.exists()) {
            isLoadingPage = true
            withContext(Dispatchers.IO) {
                val bmp = PdfHelper.renderPageToBitmap(file, currentPageIndex, maxDimension = 1400)
                withContext(Dispatchers.Main) {
                    currentPdfBitmap = bmp
                    isLoadingPage = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = doc.title,
                            maxLines = 1,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${item.subjectName} • M${item.moduleNumber}",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose, modifier = Modifier.testTag("close_viewer_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close viewer")
                    }
                },
                actions = {
                    IconButton(onClick = { onToggleStar(doc.id, doc.isStarred) }) {
                        Icon(
                            imageVector = if (doc.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Star",
                            tint = if (doc.isStarred) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { FileManager.shareDocument(context, doc) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onEditDetails) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Details")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedViewerTab) {
                Tab(
                    selected = selectedViewerTab == 0,
                    onClick = { selectedViewerTab = 0 },
                    text = { Text("Document View") }
                )
                Tab(
                    selected = selectedViewerTab == 1,
                    onClick = { selectedViewerTab = 1 },
                    text = { Text("Lecture Details") }
                )
            }

            if (selectedViewerTab == 0) {
                // Document Display View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFEFEBFF)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isPdf -> {
                            if (isLoadingPage) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            } else if (currentPdfBitmap != null) {
                                Image(
                                    bitmap = currentPdfBitmap!!.asImageBitmap(),
                                    contentDescription = "PDF Page ${currentPageIndex + 1}",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Could not render PDF preview",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(onClick = { FileManager.openFileWithExternalApp(context, doc) }) {
                                        Text("Open with PDF App")
                                    }
                                }
                            }
                        }
                        doc.fileType.equals("IMAGE", ignoreCase = true) -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(file)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Photo: ${doc.title}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "File format: ${doc.fileType}",
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(onClick = { FileManager.openFileWithExternalApp(context, doc) }) {
                                    Text("Open File")
                                }
                            }
                        }
                    }
                }

                // Controls Bar at Bottom
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPdf && doc.pageCount > 1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                                    enabled = currentPageIndex > 0
                                ) {
                                    Icon(Icons.Default.NavigateBefore, contentDescription = "Previous page")
                                }
                                Text(
                                    text = "Page ${currentPageIndex + 1} of ${doc.pageCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { if (currentPageIndex < doc.pageCount - 1) currentPageIndex++ },
                                    enabled = currentPageIndex < doc.pageCount - 1
                                ) {
                                    Icon(Icons.Default.NavigateNext, contentDescription = "Next page")
                                }
                            }
                        } else {
                            Text(
                                text = FileManager.formatFileSize(doc.fileSizeBytes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { FileManager.openFileWithExternalApp(context, doc) }
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open in App", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Lecture Details Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Organization Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Organization & Hierarchy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        }
                    }

                    // Lecture Metadata Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Lecture & Class Information",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (doc.topic.isNotBlank()) {
                                DetailRow(label = "Topic / Chapter", value = doc.topic)
                            }
                            if (doc.lecturerName.isNotBlank()) {
                                DetailRow(label = "Lecturer / Professor", value = doc.lecturerName)
                            }
                            if (doc.lectureDateString.isNotBlank()) {
                                DetailRow(label = "Lecture Date & Month", value = doc.lectureDateString)
                            }
                            if (doc.lectureTimeString.isNotBlank()) {
                                DetailRow(label = "Lecture Time", value = doc.lectureTimeString)
                            }
                            if (doc.tags.isNotBlank()) {
                                DetailRow(label = "Tags", value = doc.tags)
                            }
                        }
                    }

                    // Notes Card
                    if (doc.notes.isNotBlank()) {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Notes & Key Highlights",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = doc.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Storage & File Information Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 18.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Phone Storage & File Info",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            DetailRow(label = "File Name", value = doc.originalFileName)
                            DetailRow(label = "File Type", value = "${doc.fileType} (${doc.mimeType})")
                            DetailRow(label = "File Size", value = FileManager.formatFileSize(doc.fileSizeBytes))
                            if (isPdf) {
                                DetailRow(label = "Pages", value = "${doc.pageCount} pages")
                            }
                            DetailRow(label = "Internal Storage Path", value = file.absolutePath)
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onEditDetails,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Details")
                        }

                        Button(
                            onClick = { showDeleteConfirmation = true },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete File")
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Document?") },
            text = { Text("This will permanently delete '${doc.title}' from your phone storage.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}
