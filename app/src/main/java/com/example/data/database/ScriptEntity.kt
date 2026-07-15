package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scripts")
data class ScriptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val topic: String,
    val platform: String,
    val tone: String,
    val hookStyle: String,
    val fullText: String,
    val durationSeconds: Int,
    val isFavorite: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
