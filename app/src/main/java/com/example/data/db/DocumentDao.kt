package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentFile
import com.example.data.model.DocumentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("""
        SELECT 
            documents.*,
            subjects.name AS subjectName,
            subjects.code AS subjectCode,
            subjects.colorHex AS subjectColorHex,
            modules.moduleNumber AS moduleNumber,
            modules.name AS moduleName
        FROM documents
        INNER JOIN subjects ON documents.subjectId = subjects.id
        INNER JOIN modules ON documents.moduleId = modules.id
        ORDER BY documents.createdAt DESC
    """)
    fun getAllDocumentsWithDetails(): Flow<List<DocumentWithDetails>>

    @Query("""
        SELECT 
            documents.*,
            subjects.name AS subjectName,
            subjects.code AS subjectCode,
            subjects.colorHex AS subjectColorHex,
            modules.moduleNumber AS moduleNumber,
            modules.name AS moduleName
        FROM documents
        INNER JOIN subjects ON documents.subjectId = subjects.id
        INNER JOIN modules ON documents.moduleId = modules.id
        WHERE documents.moduleId = :moduleId
        ORDER BY documents.lectureDateMillis DESC, documents.createdAt DESC
    """)
    fun getDocumentsByModule(moduleId: Long): Flow<List<DocumentWithDetails>>

    @Query("""
        SELECT 
            documents.*,
            subjects.name AS subjectName,
            subjects.code AS subjectCode,
            subjects.colorHex AS subjectColorHex,
            modules.moduleNumber AS moduleNumber,
            modules.name AS moduleName
        FROM documents
        INNER JOIN subjects ON documents.subjectId = subjects.id
        INNER JOIN modules ON documents.moduleId = modules.id
        WHERE documents.subjectId = :subjectId
        ORDER BY modules.moduleNumber ASC, documents.createdAt DESC
    """)
    fun getDocumentsBySubject(subjectId: Long): Flow<List<DocumentWithDetails>>

    @Query("""
        SELECT 
            documents.*,
            subjects.name AS subjectName,
            subjects.code AS subjectCode,
            subjects.colorHex AS subjectColorHex,
            modules.moduleNumber AS moduleNumber,
            modules.name AS moduleName
        FROM documents
        INNER JOIN subjects ON documents.subjectId = subjects.id
        INNER JOIN modules ON documents.moduleId = modules.id
        WHERE documents.id = :id
        LIMIT 1
    """)
    fun getDocumentWithDetailsById(id: Long): Flow<DocumentWithDetails?>

    @Query("""
        SELECT 
            documents.*,
            subjects.name AS subjectName,
            subjects.code AS subjectCode,
            subjects.colorHex AS subjectColorHex,
            modules.moduleNumber AS moduleNumber,
            modules.name AS moduleName
        FROM documents
        INNER JOIN subjects ON documents.subjectId = subjects.id
        INNER JOIN modules ON documents.moduleId = modules.id
        WHERE 
            documents.title LIKE '%' || :query || '%' OR
            documents.topic LIKE '%' || :query || '%' OR
            documents.lecturerName LIKE '%' || :query || '%' OR
            documents.notes LIKE '%' || :query || '%' OR
            documents.tags LIKE '%' || :query || '%' OR
            documents.lectureDateString LIKE '%' || :query || '%' OR
            documents.originalFileName LIKE '%' || :query || '%' OR
            subjects.name LIKE '%' || :query || '%' OR
            modules.name LIKE '%' || :query || '%'
        ORDER BY documents.createdAt DESC
    """)
    fun searchDocuments(query: String): Flow<List<DocumentWithDetails>>

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getDocumentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentFile): Long

    @Update
    suspend fun updateDocument(document: DocumentFile)

    @Delete
    suspend fun deleteDocument(document: DocumentFile)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("UPDATE documents SET isStarred = :isStarred, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStarred(id: Long, isStarred: Boolean, updatedAt: Long = System.currentTimeMillis())
}
