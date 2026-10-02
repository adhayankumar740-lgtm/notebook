package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentFile
import com.example.data.model.Module
import com.example.data.model.Subject

val SUBJECT_PALETTE = listOf(
    0xFF8B5AE4, // Violet
    0xFF2286EE, // Blue
    0xFFE69400, // Amber
    0xFFE1465B, // Coral
    0xFF00917C, // Teal
    0xFF5A6168, // Slate
    0xFF652EAE, // Deep violet
    0xFF151D23  // Ink
)

@Composable
fun AddEditSubjectDialog(
    subjectToEdit: Subject?,
    onDismiss: () -> Unit,
    onSave: (name: String, code: String, colorHex: Long, description: String) -> Unit
) {
    var name by remember { mutableStateOf(subjectToEdit?.name ?: "") }
    var code by remember { mutableStateOf(subjectToEdit?.code ?: "") }
    var colorHex by remember { mutableLongStateOf(subjectToEdit?.colorHex ?: SUBJECT_PALETTE.first()) }
    var description by remember { mutableStateOf(subjectToEdit?.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (subjectToEdit == null) "New Subject" else "Edit Subject",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name *") },
                    placeholder = { Text("e.g. Operating Systems") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("subject_name_input")
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Course Code (optional)") },
                    placeholder = { Text("e.g. CS-301") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("subject_code_input")
                )

                Text(
                    text = "Subject Color:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (c in SUBJECT_PALETTE) {
                        val isSelected = c == colorHex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(c))
                                .border(
                                    width = if (isSelected) 2.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = c },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Brief course or topic overview") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("subject_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, code, colorHex, description) },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_subject_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditModuleDialog(
    moduleToEdit: Module?,
    allSubjects: List<Subject>,
    defaultSubjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (subjectId: Long, moduleNumber: Int, name: String, description: String) -> Unit
) {
    var subjectId by remember {
        mutableLongStateOf(
            moduleToEdit?.subjectId ?: defaultSubjectId ?: allSubjects.firstOrNull()?.id ?: 0L
        )
    }
    var moduleNumber by remember { mutableIntStateOf(moduleToEdit?.moduleNumber ?: 1) }
    var name by remember { mutableStateOf(moduleToEdit?.name ?: "") }
    var description by remember { mutableStateOf(moduleToEdit?.description ?: "") }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    val activeSubject = allSubjects.find { it.id == subjectId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (moduleToEdit == null) "New Module" else "Edit Module",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subject selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { subjectDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeSubject?.name ?: "Select Subject",
                            maxLines = 1
                        )
                    }
                    DropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false }
                    ) {
                        for (sub in allSubjects) {
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    subjectId = sub.id
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = moduleNumber.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let { num -> moduleNumber = num }
                    },
                    label = { Text("Module Number *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("module_number_input")
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Module Name *") },
                    placeholder = { Text("e.g. Memory Management & Paging") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("module_name_input")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Topics covered in this module") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("module_desc_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && subjectId > 0) onSave(subjectId, moduleNumber, name, description) },
                enabled = name.isNotBlank() && subjectId > 0,
                modifier = Modifier.testTag("save_module_confirm_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditDocumentDetailsDialog(
    document: DocumentFile,
    allSubjects: List<Subject>,
    allModules: List<Module>,
    onDismiss: () -> Unit,
    onSave: (
        documentId: Long,
        title: String,
        subjectId: Long,
        moduleId: Long,
        lectureDateMillis: Long?,
        lectureDateString: String,
        lectureTimeString: String,
        lecturerName: String,
        topic: String,
        notes: String,
        tags: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(document.title) }
    var subjectId by remember { mutableLongStateOf(document.subjectId) }
    val availableModules = remember(subjectId, allModules) {
        allModules.filter { it.subjectId == subjectId }
    }
    var moduleId by remember(availableModules) {
        mutableLongStateOf(
            if (availableModules.any { it.id == document.moduleId }) document.moduleId
            else availableModules.firstOrNull()?.id ?: 0L
        )
    }

    var lectureDateString by remember { mutableStateOf(document.lectureDateString) }
    var lectureTimeString by remember { mutableStateOf(document.lectureTimeString) }
    var lecturerName by remember { mutableStateOf(document.lecturerName) }
    var topic by remember { mutableStateOf(document.topic) }
    var notes by remember { mutableStateOf(document.notes) }
    var tags by remember { mutableStateOf(document.tags) }

    var subjectDropdownExpanded by remember { mutableStateOf(false) }
    var moduleDropdownExpanded by remember { mutableStateOf(false) }

    val activeSubject = allSubjects.find { it.id == subjectId }
    val activeModule = availableModules.find { it.id == moduleId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Lecture Details", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Subject & Module Dropdowns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { subjectDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = activeSubject?.name ?: "Subject", maxLines = 1, fontSize = 12.sp)
                        }
                        DropdownMenu(
                            expanded = subjectDropdownExpanded,
                            onDismissRequest = { subjectDropdownExpanded = false }
                        ) {
                            for (s in allSubjects) {
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        subjectId = s.id
                                        subjectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { moduleDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = availableModules.isNotEmpty()
                        ) {
                            Text(
                                text = activeModule?.let { "M${it.moduleNumber}" } ?: "Module",
                                maxLines = 1,
                                fontSize = 12.sp
                            )
                        }
                        DropdownMenu(
                            expanded = moduleDropdownExpanded,
                            onDismissRequest = { moduleDropdownExpanded = false }
                        ) {
                            for (m in availableModules) {
                                DropdownMenuItem(
                                    text = { Text("M${m.moduleNumber}: ${m.name}") },
                                    onClick = {
                                        moduleId = m.id
                                        moduleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lectureDateString,
                        onValueChange = { lectureDateString = it },
                        label = { Text("Date & Month") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lectureTimeString,
                        onValueChange = { lectureTimeString = it },
                        label = { Text("Time") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Chapter") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = lecturerName,
                    onValueChange = { lecturerName = it },
                    label = { Text("Lecturer / Professor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes & Highlights") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && subjectId > 0 && moduleId > 0) {
                        onSave(
                            document.id,
                            title,
                            subjectId,
                            moduleId,
                            document.lectureDateMillis,
                            lectureDateString,
                            lectureTimeString,
                            lecturerName,
                            topic,
                            notes,
                            tags
                        )
                    }
                },
                enabled = title.isNotBlank() && subjectId > 0 && moduleId > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
