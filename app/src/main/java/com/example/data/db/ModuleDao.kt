package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Module
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Query("SELECT * FROM modules ORDER BY subjectId ASC, moduleNumber ASC")
    fun getAllModules(): Flow<List<Module>>

    @Query("SELECT * FROM modules WHERE subjectId = :subjectId ORDER BY moduleNumber ASC")
    fun getModulesBySubjectId(subjectId: Long): Flow<List<Module>>

    @Query("SELECT * FROM modules WHERE subjectId = :subjectId ORDER BY moduleNumber ASC")
    suspend fun getModulesBySubjectIdDirect(subjectId: Long): List<Module>

    @Query("SELECT * FROM modules WHERE id = :id LIMIT 1")
    fun getModuleById(id: Long): Flow<Module?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: Module): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModules(modules: List<Module>): List<Long>

    @Update
    suspend fun updateModule(module: Module)

    @Delete
    suspend fun deleteModule(module: Module)

    @Query("DELETE FROM modules WHERE id = :id")
    suspend fun deleteModuleById(id: Long)
}
