package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Module
import com.example.data.model.Subject
import com.example.util.FileManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentSheet(
    allSubjects: List<Subject>,
    allModules: List<Module>,
    initialSubjectId: Long?,
    initialModuleId: Long?,
    onDismiss: () -> Unit,
    onSave: (
        uri: Uri,
        subjectId: Long,
        moduleId: Long,
        title: String,
        lectureDateMillis: Long?,
        lectureDateString: String,
        lectureTimeString: String,
        lecturerName: String,
        topic: String,
        notes: String,
        tags: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var detectedFileName by remember { mutableStateOf("") }
    var detectedFileSize by remember { mutableLongStateOf(0L) }

    var selectedSubjectId by remember {
        mutableStateOf(
            initialSubjectId ?: allSubjects.firstOrNull()?.id
        )
    }

    val availableModules = remember(selectedSubjectId, allModules) {
        if (selectedSubjectId != null) {
            allModules.filter { it.subjectId == selectedSubjectId }
        } else {
            emptyList()
        }
    }

    var selectedModuleId by remember(availableModules) {
        mutableStateOf(
            if (initialModuleId != null && availableModules.any { it.id == initialModuleId }) {
                initialModuleId
            } else {
                availableModules.firstOrNull()?.id
            }
        )
    }

    var title by remember { mutableStateOf("") }
    val defaultDate = remember {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
    }
    var lectureDateString by remember { mutableStateOf(defaultDate) }
    var lectureTimeString by remember { mutableStateOf("") }
    var lecturerName by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var moduleDropdownExpanded by remember { mutableStateOf(false) }

    // File picker launcher for PDFs or all documents
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex) ?: ""
                        detectedFileName = name
                        if (title.isBlank()) title = name.substringBeforeLast(".")
                    }
                    if (sizeIndex != -1) {
                        detectedFileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUri = uri
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex) ?: ""
                        detectedFileName = name
                        if (title.isBlank()) title = name.substringBeforeLast(".")
                    }
                    if (sizeIndex != -1) {
                        detectedFileSize = cursor.getLong(sizeIndex)
                    }
                }
            }
        }
    }

    val selectedSubject = allSubjects.find { it.id == selectedSubjectId }
    val selectedModule = availableModules.find { it.id == selectedModuleId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = com.example.ui.theme.Paper
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import Document to Phone",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // File Selection Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedUri != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (selectedUri != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = detectedFileName.ifBlank { "Selected File" },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Size: ${FileManager.formatFileSize(detectedFileSize)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { docPickerLauncher.launch(arrayOf("application/pdf", "*/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_pdf_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick PDF")
                        }

                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_photo_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Photo")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Organization: Subject & Module
            Text(
                text = "Target Folder (Subject & Module)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { subjectDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = selectedSubject?.name ?: "Select Subject",
                            maxLines = 1,
                            fontSize = 13.sp
                        )
                    }
                    DropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        for (subj in allSubjects) {
                            DropdownMenuItem(
                                text = { Text(subj.name) },
                                onClick = {
                                    selectedSubjectId = subj.id
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Module Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { moduleDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = availableModules.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = selectedModule?.let { "M${it.moduleNumber}: ${it.name}" } ?: "Select Module",
                            maxLines = 1,
                            fontSize = 13.sp
                        )
                    }
                    DropdownMenu(
                        expanded = moduleDropdownExpanded,
                        onDismissRequest = { moduleDropdownExpanded = false }
                    ) {
                        for (mod in availableModules) {
                            DropdownMenuItem(
                                text = { Text("M${mod.moduleNumber}: ${mod.name}") },
                                onClick = {
                                    selectedModuleId = mod.id
                                    moduleDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Document Details Fields
            Text(
                text = "Lecture Details & Information",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Document Title") },
                placeholder = { Text("e.g. Lecture 4 - Binary Trees") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("document_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = lectureDateString,
                    onValueChange = { lectureDateString = it },
                    label = { Text("Lecture Date") },
                    placeholder = { Text("e.g. Oct 12, 2026") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lecture_date_input")
                )

                OutlinedTextField(
                    value = lectureTimeString,
                    onValueChange = { lectureTimeString = it },
                    label = { Text("Lecture Time") },
                    placeholder = { Text("e.g. 10:30 AM") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("lecture_time_input")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Chapter") },
                placeholder = { Text("e.g. Tree Rotations & Invariants") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topic_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = lecturerName,
                onValueChange = { lecturerName = it },
                label = { Text("Lecturer / Professor") },
                placeholder = { Text("e.g. Dr. Vance") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lecturer_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. Midterm, Important, Formulas") },
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tags_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes & Key Highlights") },
                placeholder = { Text("Write any important lecture notes, key formulas, or homework reminders...") },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("notes_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            val canSave = selectedUri != null && selectedSubjectId != null && selectedModuleId != null

            Button(
                onClick = {
                    val uri = selectedUri ?: return@Button
                    val subId = selectedSubjectId ?: return@Button
                    val modId = selectedModuleId ?: return@Button
                    onSave(
                        uri,
                        subId,
                        modId,
                        title,
                        System.currentTimeMillis(),
                        lectureDateString,
                        lectureTimeString,
                        lecturerName,
                        topic,
                        notes,
                        tags
                    )
                },
                enabled = canSave,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.example.ui.theme.Ink,
                    contentColor = com.example.ui.theme.OnInk,
                    disabledContainerColor = com.example.ui.theme.SecondarySurface,
                    disabledContentColor = com.example.ui.theme.SoftIcon
                ),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_document_button")
            ) {
                Icon(
                    imageVector = Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (canSave) com.example.ui.theme.OnInk else com.example.ui.theme.SoftIcon
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save to Phone Storage",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canSave) com.example.ui.theme.OnInk else com.example.ui.theme.SoftIcon
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
