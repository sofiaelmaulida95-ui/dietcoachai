package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format YYYY-MM-DD
    val weightKg: Double,
    val bodyFatPercentage: Double? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
