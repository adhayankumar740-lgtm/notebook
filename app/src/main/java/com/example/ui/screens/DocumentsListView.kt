package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentWithDetails
import com.example.ui.components.DocumentCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassFilterChip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ImagePurple
import com.example.ui.theme.PdfRed
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.ui.viewmodel.SortOption
import com.example.util.FileManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsListView(
    viewModel: DocVaultViewModel,
    documents: List<DocumentWithDetails>,
    fileTypeFilter: String,
    starredOnly: Boolean,
    sortOption: SortOption,
    onOpenAddDocument: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Quick Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassFilterChip(
                selected = fileTypeFilter == "ALL" && !starredOnly,
                onClick = {
                    viewModel.clearFileFormats()
                    viewModel.setStarredOnly(false)
                },
                label = "All (${documents.size})"
            )

            GlassFilterChip(
                selected = fileTypeFilter == "PDF",
                onClick = {
                    viewModel.toggleFileFormat("PDF")
                },
                icon = Icons.Default.Description,
                activeColor = PdfRed,
                label = "PDFs"
            )

            GlassFilterChip(
                selected = fileTypeFilter == "IMAGE" || fileTypeFilter == "JPG",
                onClick = {
                    viewModel.toggleFileFormat("JPG")
                },
                icon = Icons.Default.Image,
                activeColor = ImagePurple,
                label = "Photos & JPG"
            )

            GlassFilterChip(
                selected = starredOnly,
                onClick = {
                    viewModel.setStarredOnly(!starredOnly)
                },
                icon = Icons.Default.Star,
                activeColor = AccentAmber,
                label = "Starred"
            )
        }

        // Subheader with Count & Sort
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${documents.size} Documents",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )

            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("sort_button")
                ) {
                    IconButton(onClick = { sortMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort Options",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = sortOption.displayName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false }
                ) {
                    for (option in SortOption.values()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayName,
                                    fontWeight = if (option == sortOption) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(option)
                                sortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (documents.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Description,
                title = "No documents found",
                description = "No documents match the current filters. Import new PDF files or photos from your phone storage.",
                actionButtonLabel = "Import Document",
                onActionClick = onOpenAddDocument
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents, key = { it.document.id }) { item ->
                    DocumentCard(
                        item = item,
                        onClick = { viewModel.openDocumentViewer(item) },
                        onToggleStar = { viewModel.toggleStar(item.document.id, item.document.isStarred) },
                        onEditDetails = { viewModel.openEditDocumentDetails(item.document) },
                        onOpenExternal = { FileManager.openFileWithExternalApp(context, item.document) },
                        onShare = { FileManager.shareDocument(context, item.document) },
                        onDelete = { viewModel.deleteDocument(item.document) }
                    )
                }
            }
        }
    }
}
