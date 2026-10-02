package com.example.data.repository

import android.content.Context
import com.example.data.db.AppDatabase
import com.example.data.model.DocumentFile
import com.example.data.model.DocumentWithDetails
import com.example.data.model.Module
import com.example.data.model.Subject
import com.example.util.FileManager
import com.example.util.PdfHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DocumentRepository(
    private val database: AppDatabase,
    private val context: Context
) {
    private val subjectDao = database.subjectDao()
    private val moduleDao = database.moduleDao()
    private val documentDao = database.documentDao()

    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allModules: Flow<List<Module>> = moduleDao.getAllModules()
    val allDocumentsWithDetails: Flow<List<DocumentWithDetails>> = documentDao.getAllDocumentsWithDetails()

    fun getModulesForSubject(subjectId: Long): Flow<List<Module>> =
        moduleDao.getModulesBySubjectId(subjectId)

    fun getDocumentsForModule(moduleId: Long): Flow<List<DocumentWithDetails>> =
        documentDao.getDocumentsByModule(moduleId)

    fun getDocumentsForSubject(subjectId: Long): Flow<List<DocumentWithDetails>> =
        documentDao.getDocumentsBySubject(subjectId)

    fun getDocumentById(id: Long): Flow<DocumentWithDetails?> =
        documentDao.getDocumentWithDetailsById(id)

    fun searchDocuments(query: String): Flow<List<DocumentWithDetails>> =
        documentDao.searchDocuments(query)

    suspend fun insertSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        subjectDao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) = withContext(Dispatchers.IO) {
        // Find all documents under this subject to clean up storage files
        val modules = moduleDao.getModulesBySubjectIdDirect(subject.id)
        for (module in modules) {
            val docs = documentDao.getDocumentsByModule(module.id)
            // Cleanup happens via cascade in DB, but let's delete files directory
            val subDir = File(context.filesDir, "documents/sub_${subject.id}")
            if (subDir.exists()) subDir.deleteRecursively()
        }
        subjectDao.deleteSubject(subject)
    }

    suspend fun insertModule(module: Module): Long = withContext(Dispatchers.IO) {
        moduleDao.insertModule(module)
    }

    suspend fun updateModule(module: Module) = withContext(Dispatchers.IO) {
        moduleDao.updateModule(module)
    }

    suspend fun deleteModule(module: Module) = withContext(Dispatchers.IO) {
        val modDir = File(context.filesDir, "documents/sub_${module.subjectId}/mod_${module.id}")
        if (modDir.exists()) modDir.deleteRecursively()
        moduleDao.deleteModule(module)
    }

    suspend fun insertDocument(document: DocumentFile): Long = withContext(Dispatchers.IO) {
        documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: DocumentFile) = withContext(Dispatchers.IO) {
        documentDao.updateDocument(document.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun toggleStar(id: Long, isStarred: Boolean) = withContext(Dispatchers.IO) {
        documentDao.updateStarred(id, isStarred)
    }

    suspend fun deleteDocument(document: DocumentFile) = withContext(Dispatchers.IO) {
        FileManager.deleteInternalFile(context, document.internalFilePath, document.thumbnailPath)
        documentDao.deleteDocument(document)
    }

    /**
     * Fresh start: Visor ships with no demo subjects, modules or documents.
     * (Kept as a no-op so the ViewModel call site stays unchanged.)
     */
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        // intentionally empty
    }
}
