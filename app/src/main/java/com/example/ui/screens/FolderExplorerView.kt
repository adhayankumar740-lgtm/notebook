package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentWithDetails
import com.example.data.model.Module
import com.example.data.model.Subject
import com.example.ui.components.DocumentCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.DocVaultViewModel
import com.example.util.FileManager

@Composable
fun FolderExplorerView(
    viewModel: DocVaultViewModel,
    selectedSubject: Subject?,
    selectedModule: Module?,
    allSubjects: List<Subject>,
    allModules: List<Module>,
    allDocuments: List<DocumentWithDetails>,
    onOpenAddDocument: (subjectId: Long?, moduleId: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(modifier = modifier.fillMaxSize()) {
        // Breadcrumb Bar
        BreadcrumbHeader(
            selectedSubject = selectedSubject,
            selectedModule = selectedModule,
            onNavigateRoot = { viewModel.selectSubject(null) },
            onNavigateSubject = { viewModel.selectModule(null) }
        )

        when {
            // Level 2: Inside a Module -> Show documents
            selectedSubject != null && selectedModule != null -> {
                val moduleDocs = allDocuments.filter { it.document.moduleId == selectedModule.id }
                ModuleDocumentsView(
                    subject = selectedSubject,
                    module = selectedModule,
                    documents = moduleDocs,
                    onAddDocument = { onOpenAddDocument(selectedSubject.id, selectedModule.id) },
                    onDocumentClick = { viewModel.openDocumentViewer(it) },
                    onToggleStar = { docId, isStarred -> viewModel.toggleStar(docId, isStarred) },
                    onEditDetails = { viewModel.openEditDocumentDetails(it.document) },
                    onOpenExternal = { FileManager.openFileWithExternalApp(context, it.document) },
                    onShare = { FileManager.shareDocument(context, it.document) },
                    onDelete = { viewModel.deleteDocument(it.document) }
                )
            }

            // Level 1: Inside a Subject -> Show modules
            selectedSubject != null -> {
                val subjectModules = allModules.filter { it.subjectId == selectedSubject.id }
                val subjectDocs = allDocuments.filter { it.document.subjectId == selectedSubject.id }
                SubjectModulesView(
                    subject = selectedSubject,
                    modules = subjectModules,
                    totalDocs = subjectDocs.size,
                    allDocs = allDocuments,
                    onSelectModule = { viewModel.selectModule(it) },
                    onAddModule = { viewModel.openAddModule(targetSubjectId = selectedSubject.id) },
                    onEditSubject = { viewModel.openAddSubject(selectedSubject) },
                    onDeleteSubject = { viewModel.deleteSubject(selectedSubject) },
                    onEditModule = { viewModel.openAddModule(moduleToEdit = it) },
                    onDeleteModule = { viewModel.deleteModule(it) },
                    onAddDocument = { onOpenAddDocument(selectedSubject.id, null) }
                )
            }

            // Level 0: All Subjects Overview
            else -> {
                SubjectsOverviewView(
                    subjects = allSubjects,
                    modules = allModules,
                    documents = allDocuments,
                    onSelectSubject = { viewModel.selectSubject(it) },
                    onAddSubject = { viewModel.openAddSubject() },
                    onEditSubject = { viewModel.openAddSubject(it) },
                    onDeleteSubject = { viewModel.deleteSubject(it) }
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbHeader(
    selectedSubject: Subject?,
    selectedModule: Module?,
    onNavigateRoot: () -> Unit,
    onNavigateSubject: () -> Unit
) {
    Surface(
        color = Color(0x99FFFFFF),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "All Subjects",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selectedSubject == null) FontWeight.Bold else FontWeight.Normal,
                color = if (selectedSubject == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(enabled = selectedSubject != null, onClick = onNavigateRoot)
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            )

            if (selectedSubject != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = selectedSubject.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selectedModule == null) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedModule == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable(enabled = selectedModule != null, onClick = onNavigateSubject)
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }

            if (selectedModule != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "Mod ${selectedModule.moduleNumber}: ${selectedModule.name}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SubjectsOverviewView(
    subjects: List<Subject>,
    modules: List<Module>,
    documents: List<DocumentWithDetails>,
    onSelectSubject: (Subject) -> Unit,
    onAddSubject: () -> Unit,
    onEditSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Hero card summarizing storage
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF6C5CE7), Color(0xFF8B5CF6), Color(0xFFFF6FA5))
                        ),
                        RoundedCornerShape(26.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Hey Student! 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your notes, PDFs & lecture photos — sab kuch ek jagah, subject-wise organized ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem(count = subjects.size.toString(), label = "Subjects")
                        StatItem(count = modules.size.toString(), label = "Modules")
                        StatItem(count = documents.size.toString(), label = "Documents")
                        val pdfCount = documents.count { it.document.fileType == "PDF" }
                        val imgCount = documents.count { it.document.fileType == "IMAGE" }
                        StatItem(count = "${pdfCount}p / ${imgCount}img", label = "Files")
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📚 My Subjects",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                FilledTonalButton(
                    onClick = onAddSubject,
                    modifier = Modifier.testTag("add_subject_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Subject")
                }
            }
        }

        if (subjects.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Folder,
                    title = "No subjects created yet",
                    description = "Create your first subject folder (e.g. Computer Science, Physics, Chemistry) to start organizing.",
                    actionButtonLabel = "Create First Subject",
                    onActionClick = onAddSubject
                )
            }
        } else {
            items(subjects, key = { it.id }) { subject ->
                val subModules = modules.filter { it.subjectId == subject.id }
                val subDocs = documents.filter { it.document.subjectId == subject.id }
                SubjectFolderCard(
                    subject = subject,
                    moduleCount = subModules.size,
                    documentCount = subDocs.size,
                    onClick = { onSelectSubject(subject) },
                    onEdit = { onEditSubject(subject) },
                    onDelete = { onDeleteSubject(subject) }
                )
            }
        }
    }
}

@Composable
private fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.85f)
        )
    }
}

@Composable
private fun SubjectFolderCard(
    subject: Subject,
    moduleCount: Int,
    documentCount: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = Color(subject.colorHex)
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}"),
        cornerRadius = 18.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder Icon with Color
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(color, color.copy(alpha = 0.55f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (subject.code.isNotBlank()) {
                        Surface(
                            color = color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = subject.code,
                                color = color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (subject.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subject.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$moduleCount modules",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "$documentCount files",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Subject options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Subject") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Subject", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
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
}

@Composable
private fun SubjectModulesView(
    subject: Subject,
    modules: List<Module>,
    totalDocs: Int,
    allDocs: List<DocumentWithDetails>,
    onSelectModule: (Module) -> Unit,
    onAddModule: () -> Unit,
    onEditSubject: () -> Unit,
    onDeleteSubject: () -> Unit,
    onEditModule: (Module) -> Unit,
    onDeleteModule: (Module) -> Unit,
    onAddDocument: () -> Unit
) {
    val color = Color(subject.colorHex)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Subject banner
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (subject.code.isNotBlank()) {
                                Text(
                                    text = subject.code,
                                    color = color,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = subject.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row {
                            IconButton(onClick = onEditSubject) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Subject")
                            }
                        }
                    }

                    if (subject.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = subject.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "${modules.size} Modules  •  $totalDocs Total Files",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Modules List",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = onAddModule,
                    modifier = Modifier.testTag("add_module_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Module")
                }
            }
        }

        if (modules.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.FolderOpen,
                    title = "No modules in this subject",
                    description = "Create Module 1, Module 2, etc. to organize your lecture notes and files.",
                    actionButtonLabel = "Create Module",
                    onActionClick = onAddModule
                )
            }
        } else {
            items(modules, key = { it.id }) { mod ->
                val modDocs = allDocs.filter { it.document.moduleId == mod.id }
                ModuleItemCard(
                    module = mod,
                    documentCount = modDocs.size,
                    accentColor = color,
                    onClick = { onSelectModule(mod) },
                    onEdit = { onEditModule(mod) },
                    onDelete = { onDeleteModule(mod) }
                )
            }
        }
    }
}

@Composable
private fun ModuleItemCard(
    module: Module,
    documentCount: Int,
    accentColor: Color,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("module_card_${module.id}"),
        cornerRadius = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Module Number Badge
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "M${module.moduleNumber}",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = module.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (module.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = module.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$documentCount files stored",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Module options")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Module") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Module", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
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
}

@Composable
private fun ModuleDocumentsView(
    subject: Subject,
    module: Module,
    documents: List<DocumentWithDetails>,
    onAddDocument: () -> Unit,
    onDocumentClick: (DocumentWithDetails) -> Unit,
    onToggleStar: (Long, Boolean) -> Unit,
    onEditDetails: (DocumentWithDetails) -> Unit,
    onOpenExternal: (DocumentWithDetails) -> Unit,
    onShare: (DocumentWithDetails) -> Unit,
    onDelete: (DocumentWithDetails) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Module header banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${subject.name} • Module ${module.moduleNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(subject.colorHex),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = module.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (module.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = module.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${documents.size} documents in module",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onAddDocument,
                            modifier = Modifier.testTag("add_file_to_module_button")
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add File")
                        }
                    }
                }
            }
        }

        if (documents.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Description,
                    title = "No files in this module yet",
                    description = "Import your lecture PDFs, classroom whiteboard photos, or study notes into this module folder.",
                    actionButtonLabel = "Import Document",
                    onActionClick = onAddDocument
                )
            }
        } else {
            items(documents, key = { it.document.id }) { item ->
                DocumentCard(
                    item = item,
                    onClick = { onDocumentClick(item) },
                    onToggleStar = { onToggleStar(item.document.id, item.document.isStarred) },
                    onEditDetails = { onEditDetails(item) },
                    onOpenExternal = { onOpenExternal(item) },
                    onShare = { onShare(item) },
                    onDelete = { onDelete(item) }
                )
            }
        }
    }
}
