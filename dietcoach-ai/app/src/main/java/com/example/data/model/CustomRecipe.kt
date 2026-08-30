package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class CustomRecipe(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Sarapan, Makan Siang, Makan Malam, Camilan Rendah Kalori
    val description: String,
    val prepTimeMinutes: Int,
    val cookTimeMinutes: Int,
    val caloriesKcal: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val ingredients: String, // Newline separated or JSON
    val instructions: String, // Newline separated steps
    val isAiGenerated: Boolean = false,
    val isFavorite: Boolean = false
)
