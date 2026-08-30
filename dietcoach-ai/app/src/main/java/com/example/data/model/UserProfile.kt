package com.example.data.model

data class UserProfile(
    val name: String = "Sofia",
    val gender: String = "Perempuan",
    val age: Int = 32,
    val initialWeightKg: Double = 67.0,
    val currentWeightKg: Double = 67.0,
    val targetWeightKg: Double = 52.0,
    val heightCm: Double = 155.0,
    val startDate: String = "2026-09-01",
    val endDate: String = "2026-12-30",
    val totalDays: Int = 121,
    val dailyCalorieTargetKcal: Int = 1400,
    val dailyCalorieBurnTargetKcal: Int = 400,
    val dailyWaterTargetMl: Int = 2200,
    val dailyStepTarget: Int = 8000
) {
    val heightInMeters: Double get() = heightCm / 100.0
    val bmi: Double get() = currentWeightKg / (heightInMeters * heightInMeters)
    val weightToLoseKg: Double get() = (currentWeightKg - targetWeightKg).coerceAtLeast(0.0)
    val totalWeightLossTargetKg: Double get() = (initialWeightKg - targetWeightKg).coerceAtLeast(0.1)

    fun getBmiCategory(): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 23.0 -> "Normal Ideal"
            bmi < 25.0 -> "Batas Atas Normal"
            bmi < 30.0 -> "Overweight"
            else -> "Obesitas"
        }
    }
}
