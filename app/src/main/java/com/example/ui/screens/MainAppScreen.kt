package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddDocumentSheet
import com.example.ui.components.AddEditModuleDialog
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.EditDocumentDetailsDialog
import com.example.ui.components.GlassBackground
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.GlassBorderLight
import com.example.ui.theme.GlassDarkSurface
import com.example.ui.theme.GlassLightSurface
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: DocVaultViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
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

    GlassBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Surface(
                    color = com.example.ui.theme.GlassSmokedBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, Color(0x1FFFFFFF), RoundedCornerShape(0.dp))
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentTab) {
                                    MainTab.EXPLORER -> "DocVault • Folders"
                                    MainTab.ALL_DOCS -> "DocVault • All Documents"
                                    MainTab.SEARCH -> "Offline Advanced Search"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.PureWhite
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            },
            bottomBar = {
                val navBarShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                Surface(
                    shape = navBarShape,
                    color = com.example.ui.theme.GlassSmokedBlack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(navBarShape)
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    Color(0x33FFFFFF),
                                    Color(0x0AFFFFFF)
                                )
                            ),
                            shape = navBarShape
                        )
                        .navigationBarsPadding()
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent
                    ) {
                        NavigationBarItem(
                            selected = currentTab == MainTab.EXPLORER,
                            onClick = { viewModel.setTab(MainTab.EXPLORER) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.EXPLORER) Icons.Filled.Folder else Icons.Outlined.Folder,
                                    contentDescription = "Folders"
                                )
                            },
                            label = { Text("Folders") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = com.example.ui.theme.PureWhite,
                                selectedIconColor = com.example.ui.theme.PureBlack,
                                unselectedIconColor = Color(0xB3FFFFFF),
                                selectedTextColor = com.example.ui.theme.PureWhite,
                                unselectedTextColor = com.example.ui.theme.Silver
                            ),
                            modifier = Modifier.testTag("tab_explorer")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.ALL_DOCS,
                            onClick = { viewModel.setTab(MainTab.ALL_DOCS) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.ALL_DOCS) Icons.Filled.Description else Icons.Outlined.Description,
                                    contentDescription = "All Docs"
                                )
                            },
                            label = { Text("All Docs") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = com.example.ui.theme.PureWhite,
                                selectedIconColor = com.example.ui.theme.PureBlack,
                                unselectedIconColor = Color(0xB3FFFFFF),
                                selectedTextColor = com.example.ui.theme.PureWhite,
                                unselectedTextColor = com.example.ui.theme.Silver
                            ),
                            modifier = Modifier.testTag("tab_all_docs")
                        )

                        NavigationBarItem(
                            selected = currentTab == MainTab.SEARCH,
                            onClick = { viewModel.setTab(MainTab.SEARCH) },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                                    contentDescription = "Search"
                                )
                            },
                            label = { Text("Search") },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = com.example.ui.theme.PureWhite,
                                selectedIconColor = com.example.ui.theme.PureBlack,
                                unselectedIconColor = Color(0xB3FFFFFF),
                                selectedTextColor = com.example.ui.theme.PureWhite,
                                unselectedTextColor = com.example.ui.theme.Silver
                            ),
                            modifier = Modifier.testTag("tab_search")
                        )
                    }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openAddDocument() },
                    containerColor = com.example.ui.theme.PureWhite,
                    contentColor = com.example.ui.theme.PureBlack,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_document")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import Document",
                        modifier = Modifier.size(24.dp)
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
