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
     * Initializes default subjects, modules, and real sample documents if database is empty.
     */
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val subjectCount = subjectDao.getSubjectCount()
        if (subjectCount > 0) return@withContext

        // Subject 1: Computer Science
        val csId = subjectDao.insertSubject(
            Subject(
                name = "Computer Science",
                code = "CS-204",
                colorHex = 0xFF4F46E5, // Indigo
                iconName = "computer",
                description = "Algorithms, Data Structures & System Architecture"
            )
        )
        val csMod1 = moduleDao.insertModule(
            Module(
                subjectId = csId,
                moduleNumber = 1,
                name = "Trees, BST & Balancing",
                description = "Binary Search Trees, AVL balance factors, rotations"
            )
        )
        val csMod2 = moduleDao.insertModule(
            Module(
                subjectId = csId,
                moduleNumber = 2,
                name = "Graph Algorithms",
                description = "Dijkstra, BFS/DFS, topological sort, minimum spanning trees"
            )
        )
        moduleDao.insertModule(
            Module(
                subjectId = csId,
                moduleNumber = 3,
                name = "Dynamic Programming",
                description = "Knapsack, Memoization, Matrix chain multiplication"
            )
        )

        // Subject 2: Applied Physics
        val physId = subjectDao.insertSubject(
            Subject(
                name = "Applied Physics",
                code = "PHY-102",
                colorHex = 0xFF0D9488, // Teal
                iconName = "science",
                description = "Classical mechanics, Electromagnetism, Quantum fundamentals"
            )
        )
        val physMod1 = moduleDao.insertModule(
            Module(
                subjectId = physId,
                moduleNumber = 1,
                name = "Electromagnetism & Maxwell",
                description = "Gauss's law, Faraday's induction, wave equations"
            )
        )
        moduleDao.insertModule(
            Module(
                subjectId = physId,
                moduleNumber = 2,
                name = "Quantum Mechanics Basics",
                description = "Schrodinger equation, wave-particle duality, potential wells"
            )
        )

        // Subject 3: Mathematics
        val mathId = subjectDao.insertSubject(
            Subject(
                name = "Engineering Mathematics",
                code = "MATH-301",
                colorHex = 0xFFEA580C, // Deep Orange
                iconName = "calculate",
                description = "Linear Algebra, Multivariable Calculus, Vector Fields"
            )
        )
        val mathMod1 = moduleDao.insertModule(
            Module(
                subjectId = mathId,
                moduleNumber = 1,
                name = "Linear Algebra & Eigenvalues",
                description = "Matrices, Eigenvectors, Diagonalization, Orthogonality"
            )
        )

        // Generate sample PDF in phone storage
        val samplePdfDir = File(context.filesDir, "documents/sub_$csId/mod_$csMod1")
        samplePdfDir.mkdirs()
        val samplePdfFile = File(samplePdfDir, "sample_avl_lecture_notes.pdf")

        val todayFormatted = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
        val pdfCreated = PdfHelper.createSampleLecturePdf(
            targetFile = samplePdfFile,
            subjectName = "Computer Science",
            moduleName = "Module 1: Trees & Balancing",
            topic = "AVL Tree Rotations and Balance Factors",
            lecturer = "Prof. Marcus Vance",
            date = todayFormatted
        )

        var thumbPath: String? = null
        if (pdfCreated) {
            val thumbDir = File(context.filesDir, "thumbnails")
            thumbDir.mkdirs()
            val thumbFile = File(thumbDir, "thumb_sample_avl.jpg")
            if (PdfHelper.renderThumbnail(samplePdfFile, thumbFile)) {
                thumbPath = "thumbnails/${thumbFile.name}"
            }

            documentDao.insertDocument(
                DocumentFile(
                    subjectId = csId,
                    moduleId = csMod1,
                    title = "AVL Tree Rotations & Invariants",
                    originalFileName = "AVL_Tree_Rotations_Lecture.pdf",
                    internalFilePath = "documents/sub_$csId/mod_$csMod1/${samplePdfFile.name}",
                    fileType = "PDF",
                    mimeType = "application/pdf",
                    fileSizeBytes = samplePdfFile.length(),
                    pageCount = 2,
                    thumbnailPath = thumbPath,
                    lectureDateMillis = System.currentTimeMillis() - 86400000L * 2,
                    lectureDateString = "Sep 28, 2026",
                    lectureTimeString = "10:30 AM - 11:45 AM",
                    lecturerName = "Prof. Marcus Vance",
                    topic = "Self-Balancing Trees & O(1) Rotations",
                    notes = "Covered balance factor calculations BF = h(L) - h(R). Detailed LL, RR, LR, and RL rotation steps. Crucial for upcoming midterm exam.",
                    tags = "Midterm, Trees, Formulas, Must-Review",
                    isStarred = true
                )
            )
        }

        // Generate sample whiteboard image in phone storage
        val sampleImgDir = File(context.filesDir, "documents/sub_$csId/mod_$csMod1")
        val sampleImgFile = File(sampleImgDir, "whiteboard_tree_balance.png")
        val imgCreated = PdfHelper.createSampleWhiteboardImage(
            targetFile = sampleImgFile,
            title = "Node 50 Balance Verification",
            date = "Sep 28, 2026"
        )
        if (imgCreated) {
            documentDao.insertDocument(
                DocumentFile(
                    subjectId = csId,
                    moduleId = csMod1,
                    title = "Whiteboard Photo - Tree Balance Check",
                    originalFileName = "whiteboard_photo_sep28.png",
                    internalFilePath = "documents/sub_$csId/mod_$csMod1/${sampleImgFile.name}",
                    fileType = "IMAGE",
                    mimeType = "image/png",
                    fileSizeBytes = sampleImgFile.length(),
                    pageCount = 1,
                    thumbnailPath = "documents/sub_$csId/mod_$csMod1/${sampleImgFile.name}",
                    lectureDateMillis = System.currentTimeMillis() - 86400000L * 2,
                    lectureDateString = "Sep 28, 2026",
                    lectureTimeString = "11:30 AM",
                    lecturerName = "Prof. Marcus Vance",
                    topic = "Whiteboard Balance Factor Diagram",
                    notes = "Photo of classroom board showing why inserting 15 causes LL imbalance at node 50.",
                    tags = "Whiteboard, Diagram, Photo",
                    isStarred = false
                )
            )
        }
    }
}
