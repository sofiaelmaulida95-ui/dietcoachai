package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format YYYY-MM-DD
    val workoutType: String, // Jalan Kaki, Lari, Bersepeda, Aerobik, Latihan Beban, HIIT
    val source: String = "Huawei Health", // Huawei Health / Manual
    val durationMinutes: Int = 30,
    val caloriesBurnedKcal: Int = 200,
    val steps: Int = 6000,
    val distanceKm: Double = 3.5,
    val avgHeartRateBpm: Int = 125,
    val coachFeedback: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
