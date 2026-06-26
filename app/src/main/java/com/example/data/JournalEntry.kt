package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val title: String,
    val content: String,
    val transcribedText: String = "",
    val videoPrompt: String = "",
    val generatedVideoUrl: String = "",
    val mood: String = "Neutral",
    val deepReflection: String = "",
    val videoAnalysis: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
