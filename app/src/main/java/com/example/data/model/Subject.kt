package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val colorHex: Long = 0xFF4F46E5, // Default Indigo
    val iconName: String = "folder",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
