package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coach_messages")
data class CoachMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSavedAsInsight: Boolean = false,
    val insightCategory: String = "Umum", // Nutrisi, Pola Makan, Olahraga, Mindset, Motivasi
    val insightNotes: String = ""
)
