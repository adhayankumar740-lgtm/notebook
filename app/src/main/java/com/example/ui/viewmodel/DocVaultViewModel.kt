package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DocumentFile
import com.example.data.model.DocumentWithDetails
import com.example.data.model.Module
import com.example.data.model.Subject
import com.example.data.repository.DocumentRepository
import com.example.util.FileManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class MainTab {
    EXPLORER,
    ALL_DOCS,
    SEARCH
}

enum class SortOption(val displayName: String) {
    NEWEST_ADDED("Recently Uploaded"),
    OLDEST_ADDED("Oldest Uploaded"),
    LECTURE_DATE_DESC("Lecture Date (Latest)"),
    LECTURE_DATE_ASC("Lecture Date (Oldest)"),
    TITLE_ASC("Title (A-Z)"),
    FILE_SIZE_DESC("File Size (Largest)")
}

enum class DateFilterType(val displayName: String) {
    CREATION_DATE("Upload / Added Date"),
    LECTURE_DATE("Lecture Date")
}

enum class DateRangePreset(val displayName: String) {
    ALL_TIME("Anytime"),
    TODAY("Today"),
    LAST_7_DAYS("Past 7 Days"),
    LAST_30_DAYS("Past 30 Days"),
    THIS_MONTH("This Month"),
    CUSTOM("Custom Range")
}

data class FilterState(
    val query: String = "",
    val selectedFormats: Set<String> = emptySet(), // "PDF", "JPG", "PNG", "WEBP"
    val dateFilterType: DateFilterType = DateFilterType.CREATION_DATE,
    val dateRangePreset: DateRangePreset = DateRangePreset.ALL_TIME,
    val customStartDateMillis: Long? = null,
    val customEndDateMillis: Long? = null,
    val subjectId: Long? = null,
    val moduleId: Long? = null,
    val selectedTags: Set<String> = emptySet(),
    val starredOnly: Boolean = false,
    val sortOption: SortOption = SortOption.NEWEST_ADDED
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() ||
                selectedFormats.isNotEmpty() ||
                dateRangePreset != DateRangePreset.ALL_TIME ||
                subjectId != null ||
                moduleId != null ||
                selectedTags.isNotEmpty() ||
                starredOnly
}

class DocVaultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DocumentRepository
    val context = application.applicationContext

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DocumentRepository(db, application.applicationContext)
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Tabs & Navigation
    private val _currentTab = MutableStateFlow(MainTab.EXPLORER)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedSubject = MutableStateFlow<Subject?>(null)
    val selectedSubject: StateFlow<Subject?> = _selectedSubject.asStateFlow()

    private val _selectedModule = MutableStateFlow<Module?>(null)
    val selectedModule: StateFlow<Module?> = _selectedModule.asStateFlow()

    // Core Data
    val allSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allModules: StateFlow<List<Module>> = repository.allModules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentWithDetails>> = repository.allDocumentsWithDetails
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unified Advanced Filter State
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val searchQuery: StateFlow<String> = _filterState.map { it.query }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val starredOnly: StateFlow<Boolean> = _filterState.map { it.starredOnly }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val sortOption: StateFlow<SortOption> = _filterState.map { it.sortOption }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SortOption.NEWEST_ADDED)

    // Active Document Viewer
    private val _viewingDocument = MutableStateFlow<DocumentWithDetails?>(null)
    val viewingDocument: StateFlow<DocumentWithDetails?> = _viewingDocument.asStateFlow()

    // Dialog & Sheet States
    private val _isAddDocumentSheetOpen = MutableStateFlow(false)
    val isAddDocumentSheetOpen: StateFlow<Boolean> = _isAddDocumentSheetOpen.asStateFlow()

    private val _addDocTargetSubjectId = MutableStateFlow<Long?>(null)
    val addDocTargetSubjectId: StateFlow<Long?> = _addDocTargetSubjectId.asStateFlow()

    private val _addDocTargetModuleId = MutableStateFlow<Long?>(null)
    val addDocTargetModuleId: StateFlow<Long?> = _addDocTargetModuleId.asStateFlow()

    private val _isAddSubjectDialogOpen = MutableStateFlow(false)
    val isAddSubjectDialogOpen: StateFlow<Boolean> = _isAddSubjectDialogOpen.asStateFlow()

    private val _editingSubject = MutableStateFlow<Subject?>(null)
    val editingSubject: StateFlow<Subject?> = _editingSubject.asStateFlow()

    private val _isAddModuleDialogOpen = MutableStateFlow(false)
    val isAddModuleDialogOpen: StateFlow<Boolean> = _isAddModuleDialogOpen.asStateFlow()

    private val _editingModule = MutableStateFlow<Module?>(null)
    val editingModule: StateFlow<Module?> = _editingModule.asStateFlow()

    private val _editingDocument = MutableStateFlow<DocumentFile?>(null)
    val editingDocument: StateFlow<DocumentFile?> = _editingDocument.asStateFlow()

    private val _isDateRangeDialogOpen = MutableStateFlow(false)
    val isDateRangeDialogOpen: StateFlow<Boolean> = _isDateRangeDialogOpen.asStateFlow()

    // Status Message / Toast
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    companion object {
        fun resolveDocumentFormat(doc: DocumentFile): String {
            val name = doc.originalFileName.lowercase()
            val mime = doc.mimeType.lowercase()
            return when {
                name.endsWith(".pdf") || mime.contains("pdf") -> "PDF"
                name.endsWith(".png") || mime.contains("png") -> "PNG"
                name.endsWith(".jpg") || name.endsWith(".jpeg") || mime.contains("jpeg") -> "JPG"
                name.endsWith(".webp") || mime.contains("webp") -> "WEBP"
                doc.fileType.equals("PDF", ignoreCase = true) -> "PDF"
                doc.fileType.equals("IMAGE", ignoreCase = true) -> "JPG"
                else -> "OTHER"
            }
        }
    }

    // Filtered documents for Search & General view
    val filteredDocuments: StateFlow<List<DocumentWithDetails>> = combine(
        allDocuments,
        _filterState
    ) { docs, filter ->
        var list = docs

        // 1. Text Query match
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { item ->
                item.document.title.lowercase().contains(q) ||
                item.document.topic.lowercase().contains(q) ||
                item.document.lecturerName.lowercase().contains(q) ||
                item.document.notes.lowercase().contains(q) ||
                item.document.tags.lowercase().contains(q) ||
                item.document.originalFileName.lowercase().contains(q) ||
                item.document.lectureDateString.lowercase().contains(q) ||
                item.subjectName.lowercase().contains(q) ||
                item.moduleName.lowercase().contains(q)
            }
        }

        // 2. Specific File Formats filter (PDF, JPG, PNG, WEBP)
        if (filter.selectedFormats.isNotEmpty()) {
            list = list.filter { item ->
                val format = resolveDocumentFormat(item.document)
                filter.selectedFormats.contains(format)
            }
        }

        // 3. Subject filter
        if (filter.subjectId != null) {
            list = list.filter { it.document.subjectId == filter.subjectId }
        }

        // 4. Module filter
        if (filter.moduleId != null) {
            list = list.filter { it.document.moduleId == filter.moduleId }
        }

        // 5. Custom Tags Multi-selection filter
        if (filter.selectedTags.isNotEmpty()) {
            val targetTags = filter.selectedTags.map { it.lowercase() }
            list = list.filter { item ->
                val docTags = item.document.tags.split(",").map { it.trim().lowercase() }
                targetTags.any { target -> docTags.contains(target) }
            }
        }

        // 6. Date Range filter (Upload / Creation Date vs Lecture Date)
        if (filter.dateRangePreset != DateRangePreset.ALL_TIME) {
            val (rangeStart, rangeEnd) = calculateDateRangeBounds(filter)
            if (rangeStart != null && rangeEnd != null) {
                list = list.filter { item ->
                    val timestamp = if (filter.dateFilterType == DateFilterType.CREATION_DATE) {
                        item.document.createdAt
                    } else {
                        item.document.lectureDateMillis ?: item.document.createdAt
                    }
                    timestamp in rangeStart..rangeEnd
                }
            }
        }

        // 7. Starred filter
        if (filter.starredOnly) {
            list = list.filter { it.document.isStarred }
        }

        // 8. Sorting
        when (filter.sortOption) {
            SortOption.NEWEST_ADDED -> list.sortedByDescending { it.document.createdAt }
            SortOption.OLDEST_ADDED -> list.sortedBy { it.document.createdAt }
            SortOption.LECTURE_DATE_DESC -> list.sortedByDescending { it.document.lectureDateMillis ?: it.document.createdAt }
            SortOption.LECTURE_DATE_ASC -> list.sortedBy { it.document.lectureDateMillis ?: it.document.createdAt }
            SortOption.TITLE_ASC -> list.sortedBy { it.document.title.lowercase() }
            SortOption.FILE_SIZE_DESC -> list.sortedByDescending { it.document.fileSizeBytes }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All unique tags across documents
    val allTags: StateFlow<List<String>> = allDocuments.map { docs ->
        docs.flatMap { it.document.tags.split(",") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun calculateDateRangeBounds(filter: FilterState): Pair<Long?, Long?> {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        return when (filter.dateRangePreset) {
            DateRangePreset.ALL_TIME -> Pair(null, null)
            DateRangePreset.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.LAST_7_DAYS -> {
                val start = now - (7L * 24 * 60 * 60 * 1000)
                Pair(start, now + 1000)
            }
            DateRangePreset.LAST_30_DAYS -> {
                val start = now - (30L * 24 * 60 * 60 * 1000)
                Pair(start, now + 1000)
            }
            DateRangePreset.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            DateRangePreset.CUSTOM -> {
                Pair(filter.customStartDateMillis, filter.customEndDateMillis)
            }
        }
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectSubject(subject: Subject?) {
        _selectedSubject.value = subject
        _selectedModule.value = null
    }

    fun selectModule(module: Module?) {
        _selectedModule.value = module
    }

    fun handleBackPress(): Boolean {
        if (_isDateRangeDialogOpen.value) {
            _isDateRangeDialogOpen.value = false
            return true
        }
        if (_viewingDocument.value != null) {
            _viewingDocument.value = null
            return true
        }
        if (_editingDocument.value != null) {
            _editingDocument.value = null
            return true
        }
        if (_selectedModule.value != null) {
            _selectedModule.value = null
            return true
        }
        if (_selectedSubject.value != null) {
            _selectedSubject.value = null
            return true
        }
        if (_currentTab.value != MainTab.EXPLORER) {
            _currentTab.value = MainTab.EXPLORER
            return true
        }
        return false
    }

    // Advanced Search Actions
    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(query = query)
    }

    fun toggleFileFormat(format: String) {
        val current = _filterState.value.selectedFormats.toMutableSet()
        if (current.contains(format)) {
            current.remove(format)
        } else {
            current.add(format)
        }
        _filterState.value = _filterState.value.copy(selectedFormats = current)
    }

    fun clearFileFormats() {
        _filterState.value = _filterState.value.copy(selectedFormats = emptySet())
    }

    fun setDateFilterType(type: DateFilterType) {
        _filterState.value = _filterState.value.copy(dateFilterType = type)
    }

    fun setDateRangePreset(preset: DateRangePreset, customStart: Long? = null, customEnd: Long? = null) {
        _filterState.value = _filterState.value.copy(
            dateRangePreset = preset,
            customStartDateMillis = customStart ?: _filterState.value.customStartDateMillis,
            customEndDateMillis = customEnd ?: _filterState.value.customEndDateMillis
        )
    }

    fun openDateRangeDialog() {
        _isDateRangeDialogOpen.value = true
    }

    fun closeDateRangeDialog() {
        _isDateRangeDialogOpen.value = false
    }

    fun setSubjectFilterId(id: Long?) {
        _filterState.value = _filterState.value.copy(subjectId = id, moduleId = null)
    }

    fun setModuleFilterId(id: Long?) {
        _filterState.value = _filterState.value.copy(moduleId = id)
    }

    fun toggleTagFilter(tag: String) {
        val current = _filterState.value.selectedTags.toMutableSet()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _filterState.value = _filterState.value.copy(selectedTags = current)
    }

    fun clearTags() {
        _filterState.value = _filterState.value.copy(selectedTags = emptySet())
    }

    fun setStarredOnly(starred: Boolean) {
        _filterState.value = _filterState.value.copy(starredOnly = starred)
    }

    fun setSortOption(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort)
    }

    fun clearSearchFilters() {
        _filterState.value = FilterState()
    }

    fun openDocumentViewer(doc: DocumentWithDetails) {
        _viewingDocument.value = doc
    }

    fun closeDocumentViewer() {
        _viewingDocument.value = null
    }

    fun openAddDocument(targetSubjectId: Long? = null, targetModuleId: Long? = null) {
        _addDocTargetSubjectId.value = targetSubjectId ?: _selectedSubject.value?.id
        _addDocTargetModuleId.value = targetModuleId ?: _selectedModule.value?.id
        _isAddDocumentSheetOpen.value = true
    }

    fun closeAddDocument() {
        _isAddDocumentSheetOpen.value = false
    }

    fun openAddSubject(subjectToEdit: Subject? = null) {
        _editingSubject.value = subjectToEdit
        _isAddSubjectDialogOpen.value = true
    }

    fun closeAddSubject() {
        _editingSubject.value = null
        _isAddSubjectDialogOpen.value = false
    }

    fun openAddModule(moduleToEdit: Module? = null, targetSubjectId: Long? = null) {
        _editingModule.value = moduleToEdit
        if (targetSubjectId != null) {
            val sub = allSubjects.value.find { it.id == targetSubjectId }
            if (sub != null) _selectedSubject.value = sub
        }
        _isAddModuleDialogOpen.value = true
    }

    fun closeAddModule() {
        _editingModule.value = null
        _isAddModuleDialogOpen.value = false
    }

    fun openEditDocumentDetails(document: DocumentFile) {
        _editingDocument.value = document
    }

    fun closeEditDocumentDetails() {
        _editingDocument.value = null
    }

    fun showMessage(msg: String) {
        _statusMessage.value = msg
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun toggleStar(docId: Long, currentStar: Boolean) {
        viewModelScope.launch {
            repository.toggleStar(docId, !currentStar)
            _viewingDocument.value?.let { current ->
                if (current.document.id == docId) {
                    _viewingDocument.value = current.copy(
                        document = current.document.copy(isStarred = !currentStar)
                    )
                }
            }
        }
    }

    fun deleteDocument(doc: DocumentFile) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            if (_viewingDocument.value?.document?.id == doc.id) {
                _viewingDocument.value = null
            }
            showMessage("Document removed from storage")
        }
    }

    fun saveSubject(name: String, code: String, colorHex: Long, description: String) {
        viewModelScope.launch {
            val current = _editingSubject.value
            if (current != null) {
                repository.updateSubject(
                    current.copy(
                        name = name.trim(),
                        code = code.trim(),
                        colorHex = colorHex,
                        description = description.trim()
                    )
                )
                showMessage("Subject updated")
            } else {
                repository.insertSubject(
                    Subject(
                        name = name.trim(),
                        code = code.trim(),
                        colorHex = colorHex,
                        description = description.trim()
                    )
                )
                showMessage("Subject created")
            }
            closeAddSubject()
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            if (_selectedSubject.value?.id == subject.id) {
                _selectedSubject.value = null
                _selectedModule.value = null
            }
            repository.deleteSubject(subject)
            showMessage("Subject and contents deleted")
        }
    }

    fun saveModule(subjectId: Long, moduleNumber: Int, name: String, description: String) {
        viewModelScope.launch {
            val current = _editingModule.value
            if (current != null) {
                repository.updateModule(
                    current.copy(
                        moduleNumber = moduleNumber,
                        name = name.trim(),
                        description = description.trim()
                    )
                )
                showMessage("Module updated")
            } else {
                repository.insertModule(
                    Module(
                        subjectId = subjectId,
                        moduleNumber = moduleNumber,
                        name = name.trim(),
                        description = description.trim()
                    )
                )
                showMessage("Module created")
            }
            closeAddModule()
        }
    }

    fun deleteModule(module: Module) {
        viewModelScope.launch {
            if (_selectedModule.value?.id == module.id) {
                _selectedModule.value = null
            }
            repository.deleteModule(module)
            showMessage("Module deleted")
        }
    }

    fun importFileAndSave(
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
    ) {
        viewModelScope.launch {
            val imported = FileManager.importFileFromUri(context, uri, subjectId, moduleId)
            if (imported == null) {
                showMessage("Failed to import file. Please check file access.")
                return@launch
            }

            val finalTitle = if (title.isNotBlank()) title.trim() else imported.originalFileName
            val dateStr = if (lectureDateString.isNotBlank()) {
                lectureDateString
            } else if (lectureDateMillis != null) {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(lectureDateMillis))
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
            }

            val newDoc = DocumentFile(
                subjectId = subjectId,
                moduleId = moduleId,
                title = finalTitle,
                originalFileName = imported.originalFileName,
                internalFilePath = imported.relativeInternalPath,
                fileType = imported.fileType,
                mimeType = imported.mimeType,
                fileSizeBytes = imported.fileSizeBytes,
                pageCount = imported.pageCount,
                thumbnailPath = imported.thumbnailPath,
                lectureDateMillis = lectureDateMillis ?: System.currentTimeMillis(),
                lectureDateString = dateStr,
                lectureTimeString = lectureTimeString.trim(),
                lecturerName = lecturerName.trim(),
                topic = topic.trim(),
                notes = notes.trim(),
                tags = tags.trim()
            )

            repository.insertDocument(newDoc)
            showMessage("File saved in phone storage ($finalTitle)")
            closeAddDocument()
        }
    }

    fun updateDocumentDetails(
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
    ) {
        viewModelScope.launch {
            val existing = allDocuments.value.find { it.document.id == documentId }?.document ?: return@launch
            val updated = existing.copy(
                title = title.trim(),
                subjectId = subjectId,
                moduleId = moduleId,
                lectureDateMillis = lectureDateMillis ?: existing.lectureDateMillis,
                lectureDateString = lectureDateString.trim(),
                lectureTimeString = lectureTimeString.trim(),
                lecturerName = lecturerName.trim(),
                topic = topic.trim(),
                notes = notes.trim(),
                tags = tags.trim(),
                updatedAt = System.currentTimeMillis()
            )
            repository.updateDocument(updated)

            _viewingDocument.value?.let { current ->
                if (current.document.id == documentId) {
                    val sub = allSubjects.value.find { it.id == subjectId }
                    val mod = allModules.value.find { it.id == moduleId }
                    _viewingDocument.value = current.copy(
                        document = updated,
                        subjectName = sub?.name ?: current.subjectName,
                        subjectCode = sub?.code ?: current.subjectCode,
                        subjectColorHex = sub?.colorHex ?: current.subjectColorHex,
                        moduleNumber = mod?.moduleNumber ?: current.moduleNumber,
                        moduleName = mod?.name ?: current.moduleName
                    )
                }
            }
            closeEditDocumentDetails()
            showMessage("Lecture details updated")
        }
    }
}
