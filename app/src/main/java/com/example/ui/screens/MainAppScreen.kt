package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddDocumentSheet
import com.example.ui.components.AddEditModuleDialog
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.EditDocumentDetailsDialog
import com.example.ui.components.GlassBackground
import com.example.ui.theme.BorderColor
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DisplayFont
import com.example.ui.theme.Ink
import com.example.ui.theme.MutedFg
import com.example.ui.theme.OnInk
import com.example.ui.theme.Paper
import com.example.ui.theme.Violet
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun MainAppScreen(
    viewModel: DocVaultViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val selectedModule by viewModel.selectedModule.collectAsStateWithLifecycle()

    val allSubjects by viewModel.allSubjects.collectAsStateWithLifecycle()
    val allModules by viewModel.allModules.collectAsStateWithLifecycle()
    val allDocuments by viewModel.allDocuments.collectAsStateWithLifecycle()
    val filteredDocuments by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val starredOnly by viewModel.starredOnly.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()

    val viewingDocument by viewModel.viewingDocument.collectAsStateWithLifecycle()
    val isAddDocOpen by viewModel.isAddDocumentSheetOpen.collectAsStateWithLifecycle()
    val targetSubId by viewModel.addDocTargetSubjectId.collectAsStateWithLifecycle()
    val targetModId by viewModel.addDocTargetModuleId.collectAsStateWithLifecycle()

    val isAddSubjectOpen by viewModel.isAddSubjectDialogOpen.collectAsStateWithLifecycle()
    val editingSubject by viewModel.editingSubject.collectAsStateWithLifecycle()

    val isAddModuleOpen by viewModel.isAddModuleDialogOpen.collectAsStateWithLifecycle()
    val editingModule by viewModel.editingModule.collectAsStateWithLifecycle()

    val editingDoc by viewModel.editingDocument.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Hardware back navigation handler
    BackHandler(enabled = true) {
        if (!viewModel.handleBackPress()) {
            // Default back
        }
    }

    // If viewing document fullscreen
    if (viewingDocument != null) {
        DocumentViewerScreen(
            item = viewingDocument!!,
            onClose = { viewModel.closeDocumentViewer() },
            onToggleStar = { id, starred -> viewModel.toggleStar(id, starred) },
            onEditDetails = { viewModel.openEditDocumentDetails(viewingDocument!!.document) },
            onDelete = { viewModel.deleteDocument(viewingDocument!!.document) }
        )
        return
    }

    val onFolders = currentTab == MainTab.EXPLORER
    val onAll = currentTab == MainTab.ALL_DOCS && !starredOnly
    val onStarred = currentTab == MainTab.ALL_DOCS && starredOnly
    val onSearch = currentTab == MainTab.SEARCH

    GlassBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DocVaultHeader(
                    onHome = {
                        viewModel.selectSubject(null)
                        viewModel.setTab(MainTab.EXPLORER)
                    },
                    onAdd = { viewModel.openAddDocument() }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = CardSurface,
                    tonalElevation = 0.dp
                ) {
                    DocVaultNavItem(
                        selected = onFolders,
                        label = "Folders",
                        selectedIcon = Icons.Filled.Folder,
                        icon = Icons.Outlined.Folder,
                        testTag = "tab_explorer",
                        onClick = { viewModel.setTab(MainTab.EXPLORER) }
                    )
                    DocVaultNavItem(
                        selected = onAll,
                        label = "All",
                        selectedIcon = Icons.Filled.Description,
                        icon = Icons.Outlined.Description,
                        testTag = "tab_all_docs",
                        onClick = {
                            viewModel.clearSearchFilters()
                            viewModel.setTab(MainTab.ALL_DOCS)
                        }
                    )
                    DocVaultNavItem(
                        selected = onStarred,
                        label = "Starred",
                        selectedIcon = Icons.Filled.Star,
                        icon = Icons.Outlined.StarBorder,
                        testTag = "tab_starred",
                        onClick = {
                            viewModel.clearSearchFilters()
                            viewModel.setStarredOnly(true)
                            viewModel.setTab(MainTab.ALL_DOCS)
                        }
                    )
                    DocVaultNavItem(
                        selected = onSearch,
                        label = "Search",
                        selectedIcon = Icons.Filled.Search,
                        icon = Icons.Outlined.Search,
                        testTag = "tab_search",
                        onClick = {
                            if (starredOnly) viewModel.setStarredOnly(false)
                            viewModel.setTab(MainTab.SEARCH)
                        }
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    MainTab.EXPLORER -> {
                        FolderExplorerView(
                            viewModel = viewModel,
                            selectedSubject = selectedSubject,
                            selectedModule = selectedModule,
                            allSubjects = allSubjects,
                            allModules = allModules,
                            allDocuments = allDocuments,
                            onOpenAddDocument = { subId, modId -> viewModel.openAddDocument(subId, modId) }
                        )
                    }

                    MainTab.ALL_DOCS -> {
                        DocumentsListView(
                            viewModel = viewModel,
                            documents = filteredDocuments,
                            fileTypeFilter = filterState.selectedFormats.firstOrNull() ?: "ALL",
                            starredOnly = starredOnly,
                            sortOption = sortOption,
                            onOpenAddDocument = { viewModel.openAddDocument() }
                        )
                    }

                    MainTab.SEARCH -> {
                        OfflineSearchView(
                            viewModel = viewModel,
                            searchQuery = searchQuery,
                            filterState = filterState,
                            filteredDocuments = filteredDocuments,
                            allSubjects = allSubjects,
                            allModules = allModules,
                            allTags = allTags
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet: Add Document
    if (isAddDocOpen) {
        AddDocumentSheet(
            allSubjects = allSubjects,
            allModules = allModules,
            initialSubjectId = targetSubId,
            initialModuleId = targetModId,
            onDismiss = { viewModel.closeAddDocument() },
            onSave = { uri, subId, modId, title, dateMillis, dateStr, timeStr, lecturer, topic, notes, tags ->
                viewModel.importFileAndSave(
                    uri, subId, modId, title, dateMillis, dateStr, timeStr, lecturer, topic, notes, tags
                )
            }
        )
    }

    // Dialog: Add/Edit Subject
    if (isAddSubjectOpen) {
        AddEditSubjectDialog(
            subjectToEdit = editingSubject,
            onDismiss = { viewModel.closeAddSubject() },
            onSave = { name, code, colorHex, desc ->
                viewModel.saveSubject(name, code, colorHex, desc)
            }
        )
    }

    // Dialog: Add/Edit Module
    if (isAddModuleOpen) {
        AddEditModuleDialog(
            moduleToEdit = editingModule,
            allSubjects = allSubjects,
            defaultSubjectId = selectedSubject?.id,
            onDismiss = { viewModel.closeAddModule() },
            onSave = { subId, modNum, name, desc ->
                viewModel.saveModule(subId, modNum, name, desc)
            }
        )
    }

    // Dialog: Edit Document Details
    if (editingDoc != null) {
        EditDocumentDetailsDialog(
            document = editingDoc!!,
            allSubjects = allSubjects,
            allModules = allModules,
            onDismiss = { viewModel.closeEditDocumentDetails() },
            onSave = { id, title, subId, modId, dateMillis, dateStr, timeStr, lecturer, topic, notes, tags ->
                viewModel.updateDocumentDetails(
                    id, title, subId, modId, dateMillis, dateStr, timeStr, lecturer, topic, notes, tags
                )
            }
        )
    }
}

@Composable
private fun DocVaultHeader(
    onHome: () -> Unit,
    onAdd: () -> Unit
) {
    Surface(
        color = Paper,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                // Logo + wordmark
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onHome)
                        .testTag("app_logo")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Violet)
                    ) {
                        Text(
                            text = "D",
                            color = OnInk,
                            fontFamily = DisplayFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "DocVault",
                        color = Ink,
                        fontFamily = DisplayFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                }

                // "Add" pill
                Surface(
                    color = Ink,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onAdd)
                        .testTag("fab_add_document")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NoteAdd,
                            contentDescription = "Add document",
                            tint = OnInk,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add",
                            color = OnInk,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderColor)
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.DocVaultNavItem(
    selected: Boolean,
    label: String,
    selectedIcon: ImageVector,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = label
            )
        },
        label = { Text(label, fontSize = 11.sp) },
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = Violet,
            selectedTextColor = Violet,
            unselectedIconColor = MutedFg,
            unselectedTextColor = MutedFg
        ),
        modifier = Modifier.testTag(testTag)
    )
}
