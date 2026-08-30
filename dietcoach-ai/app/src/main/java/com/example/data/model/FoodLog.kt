package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_logs")
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // format YYYY-MM-DD
    val mealType: String, // Sarapan, Makan Siang, Makan Malam, Camilan
    val foodName: String,
    val portionDescription: String = "1 porsi",
    val caloriesKcal: Int,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val healthScore: Int = 8, // 1 to 10
    val coachVerdict: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
