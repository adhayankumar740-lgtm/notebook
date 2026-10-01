package com.example.data.model

import androidx.room.Embedded

data class DocumentWithDetails(
    @Embedded val document: DocumentFile,
    val subjectName: String,
    val subjectCode: String,
    val subjectColorHex: Long,
    val moduleNumber: Int,
    val moduleName: String
)
