package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentWithDetails
import com.example.data.model.Module
import com.example.data.model.Subject
import com.example.ui.components.DateRangePickerDialog
import com.example.ui.components.DocumentCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassFilterChip
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GlassViolet
import com.example.ui.theme.ImagePurple
import com.example.ui.theme.PdfRed
import com.example.ui.theme.PngTeal
import com.example.ui.theme.WebpAmber
import com.example.ui.viewmodel.DateFilterType
import com.example.ui.viewmodel.DateRangePreset
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.SortOption
import com.example.util.FileManager

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OfflineSearchView(
    viewModel: DocVaultViewModel,
    searchQuery: String,
    filterState: FilterState,
    filteredDocuments: List<DocumentWithDetails>,
    allSubjects: List<Subject>,
    allModules: List<Module>,
    allTags: List<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFilterPanelExpanded by remember { mutableStateOf(true) }
    var isDateRangeDialogOpen by remember { mutableStateOf(false) }

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var moduleMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    val activeSubject = allSubjects.find { it.id == filterState.subjectId }
    val relevantModules = if (filterState.subjectId != null) {
        allModules.filter { it.subjectId == filterState.subjectId }
    } else {
        allModules
    }
    val activeModule = allModules.find { it.id == filterState.moduleId }

    // Count of active filter rules (excluding text query)
    val activeRuleCount = (if (filterState.selectedFormats.isNotEmpty()) 1 else 0) +
            (if (filterState.dateRangePreset != DateRangePreset.ALL_TIME) 1 else 0) +
            (if (filterState.selectedTags.isNotEmpty()) filterState.selectedTags.size else 0) +
            (if (filterState.subjectId != null) 1 else 0) +
            (if (filterState.moduleId != null) 1 else 0) +
            (if (filterState.starredOnly) 1 else 0)

    Column(modifier = modifier.fillMaxSize()) {
        // Frosted Glass Search & Filter Control Panel
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input"),
                    placeholder = {
                        Text(
                            text = "Search offline: title, topic, notes, date...",
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.testTag("clear_search_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                            IconButton(
                                onClick = { isFilterPanelExpanded = !isFilterPanelExpanded },
                                modifier = Modifier.testTag("toggle_filters_panel_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (activeRuleCount > 0) {
                                            Badge { Text(activeRuleCount.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFilterPanelExpanded) Icons.Default.Tune else Icons.Default.FilterAlt,
                                        contentDescription = "Toggle Filters",
                                        tint = if (activeRuleCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Expandable Advanced Filters Section
                AnimatedVisibility(
                    visible = isFilterPanelExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        // Section 1: File Formats (PDF, JPG, PNG, WEBP)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "File Formats:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (filterState.selectedFormats.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${filterState.selectedFormats.size} selected)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassFilterChip(
                                selected = filterState.selectedFormats.isEmpty(),
                                onClick = { viewModel.clearFileFormats() },
                                label = "All Types"
                            )

                            GlassFilterChip(
                                selected = filterState.selectedFormats.contains("PDF"),
                                onClick = { viewModel.toggleFileFormat("PDF") },
                                label = "PDF",
                                icon = Icons.Default.Description,
                                activeColor = PdfRed
                            )

                            GlassFilterChip(
                                selected = filterState.selectedFormats.contains("JPG"),
                                onClick = { viewModel.toggleFileFormat("JPG") },
                                label = "JPG",
                                icon = Icons.Default.PhotoLibrary,
                                activeColor = ImagePurple
                            )

                            GlassFilterChip(
                                selected = filterState.selectedFormats.contains("PNG"),
                                onClick = { viewModel.toggleFileFormat("PNG") },
                                label = "PNG",
                                icon = Icons.Default.Image,
                                activeColor = PngTeal
                            )

                            GlassFilterChip(
                                selected = filterState.selectedFormats.contains("WEBP"),
                                onClick = { viewModel.toggleFileFormat("WEBP") },
                                label = "WEBP",
                                icon = Icons.Default.Image,
                                activeColor = WebpAmber
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 2: Date Range Filter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Date Range:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${filterState.dateFilterType.displayName})",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(
                                onClick = { isDateRangeDialogOpen = true },
                                modifier = Modifier.testTag("open_date_dialog_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Custom Range", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassFilterChip(
                                selected = filterState.dateRangePreset == DateRangePreset.ALL_TIME,
                                onClick = { viewModel.setDateRangePreset(DateRangePreset.ALL_TIME) },
                                label = "Anytime"
                            )

                            GlassFilterChip(
                                selected = filterState.dateRangePreset == DateRangePreset.TODAY,
                                onClick = { viewModel.setDateRangePreset(DateRangePreset.TODAY) },
                                label = "Today",
                                activeColor = GlassCyan
                            )

                            GlassFilterChip(
                                selected = filterState.dateRangePreset == DateRangePreset.LAST_7_DAYS,
                                onClick = { viewModel.setDateRangePreset(DateRangePreset.LAST_7_DAYS) },
                                label = "Past 7 Days",
                                activeColor = GlassCyan
                            )

                            GlassFilterChip(
                                selected = filterState.dateRangePreset == DateRangePreset.LAST_30_DAYS,
                                onClick = { viewModel.setDateRangePreset(DateRangePreset.LAST_30_DAYS) },
                                label = "Past 30 Days",
                                activeColor = GlassCyan
                            )

                            GlassFilterChip(
                                selected = filterState.dateRangePreset == DateRangePreset.THIS_MONTH,
                                onClick = { viewModel.setDateRangePreset(DateRangePreset.THIS_MONTH) },
                                label = "This Month",
                                activeColor = GlassCyan
                            )

                            if (filterState.dateRangePreset == DateRangePreset.CUSTOM) {
                                GlassFilterChip(
                                    selected = true,
                                    onClick = { isDateRangeDialogOpen = true },
                                    label = "Custom Dates",
                                    activeColor = GlassViolet
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Section 3: Custom Tags Filter
                        if (allTags.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Custom Tags (${allTags.size}):",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (filterState.selectedTags.isNotEmpty()) {
                                    TextButton(onClick = { viewModel.clearTags() }) {
                                        Text("Clear Tags", fontSize = 11.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (tag in allTags) {
                                    val isSelected = filterState.selectedTags.contains(tag)
                                    GlassFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.toggleTagFilter(tag) },
                                        label = "#$tag",
                                        activeColor = GlassViolet
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // Section 4: Folder Scope & Starred & Sort
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Subject Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { subjectMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = activeSubject?.name ?: "All Subjects",
                                        maxLines = 1,
                                        fontSize = 12.sp
                                    )
                                }
                                DropdownMenu(
                                    expanded = subjectMenuExpanded,
                                    onDismissRequest = { subjectMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Subjects") },
                                        onClick = {
                                            viewModel.setSubjectFilterId(null)
                                            subjectMenuExpanded = false
                                        }
                                    )
                                    for (subj in allSubjects) {
                                        DropdownMenuItem(
                                            text = { Text(subj.name) },
                                            onClick = {
                                                viewModel.setSubjectFilterId(subj.id)
                                                subjectMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Module Dropdown
                            Box(modifier = Modifier.weight(1f)) {
                                OutlinedButton(
                                    onClick = { moduleMenuExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = relevantModules.isNotEmpty(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (activeModule != null) "M${activeModule.moduleNumber}: ${activeModule.name}" else "All Modules",
                                        maxLines = 1,
                                        fontSize = 12.sp
                                    )
                                }
                                DropdownMenu(
                                    expanded = moduleMenuExpanded,
                                    onDismissRequest = { moduleMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("All Modules") },
                                        onClick = {
                                            viewModel.setModuleFilterId(null)
                                            moduleMenuExpanded = false
                                        }
                                    )
                                    for (mod in relevantModules) {
                                        DropdownMenuItem(
                                            text = { Text("M${mod.moduleNumber}: ${mod.name}") },
                                            onClick = {
                                                viewModel.setModuleFilterId(mod.id)
                                                moduleMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Starred & Sort Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassFilterChip(
                                selected = filterState.starredOnly,
                                onClick = { viewModel.setStarredOnly(!filterState.starredOnly) },
                                label = "Starred Only",
                                icon = Icons.Default.Star,
                                activeColor = AccentAmber
                            )

                            Box {
                                OutlinedButton(
                                    onClick = { sortMenuExpanded = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = filterState.sortOption.displayName, fontSize = 11.sp)
                                }

                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    for (opt in SortOption.values()) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = opt.displayName,
                                                    fontWeight = if (opt == filterState.sortOption) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                viewModel.setSortOption(opt)
                                                sortMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Reset All Filters Button
                        if (filterState.hasActiveFilters) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { viewModel.clearSearchFilters() },
                                    modifier = Modifier.testTag("reset_search_filters_button")
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reset All Filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Criteria Badges Row (when panel is collapsed or summarizing)
        if (filterState.hasActiveFilters && !isFilterPanelExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Active:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                for (fmt in filterState.selectedFormats) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.toggleFileFormat(fmt) },
                        label = { Text(fmt, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
                if (filterState.dateRangePreset != DateRangePreset.ALL_TIME) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.setDateRangePreset(DateRangePreset.ALL_TIME) },
                        label = { Text(filterState.dateRangePreset.displayName, fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
                for (tag in filterState.selectedTags) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.toggleTagFilter(tag) },
                        label = { Text("#$tag", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
                if (filterState.subjectId != null) {
                    InputChip(
                        selected = true,
                        onClick = { viewModel.setSubjectFilterId(null) },
                        label = { Text(activeSubject?.name ?: "Subject", fontSize = 11.sp) },
                        trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(12.dp)) }
                    )
                }
            }
        }

        // Result Statistics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val countText = if (filterState.hasActiveFilters) {
                "${filteredDocuments.size} matching documents"
            } else {
                "${filteredDocuments.size} total documents stored offline"
            }
            Text(
                text = countText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "100% Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        // Results List
        if (filteredDocuments.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.SearchOff,
                title = "No documents found",
                description = "Try adjusting your file format, date range, or tag filters to find your files.",
                actionButtonLabel = "Reset Filters",
                onActionClick = { viewModel.clearSearchFilters() }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredDocuments, key = { it.document.id }) { item ->
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

    if (isDateRangeDialogOpen) {
        DateRangePickerDialog(
            currentPreset = filterState.dateRangePreset,
            currentDateType = filterState.dateFilterType,
            customStartMillis = filterState.customStartDateMillis,
            customEndMillis = filterState.customEndDateMillis,
            onDismiss = { isDateRangeDialogOpen = false },
            onApply = { preset, dateType, customStart, customEnd ->
                viewModel.setDateFilterType(dateType)
                viewModel.setDateRangePreset(preset, customStart, customEnd)
                isDateRangeDialogOpen = false
            }
        )
    }
}
