package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class DietRepository(private val database: AppDatabase) {

    private val foodDao = database.foodLogDao()
    private val workoutDao = database.workoutLogDao()
    private val waterDao = database.waterLogDao()
    private val weightDao = database.weightLogDao()
    private val coachDao = database.coachMessageDao()
    private val recipeDao = database.recipeDao()

    // Food Logs
    fun getAllFoodLogs(): Flow<List<FoodLog>> = foodDao.getAllFoodLogs()
    fun getFoodLogsByDate(date: String): Flow<List<FoodLog>> = foodDao.getFoodLogsByDate(date)
    fun getFoodLogsBetween(startDate: String, endDate: String): Flow<List<FoodLog>> = foodDao.getFoodLogsBetweenDates(startDate, endDate)
    suspend fun insertFoodLog(foodLog: FoodLog): Long = foodDao.insertFoodLog(foodLog)
    suspend fun deleteFoodLog(foodLog: FoodLog) = foodDao.deleteFoodLog(foodLog)

    // Workout Logs
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>> = workoutDao.getAllWorkoutLogs()
    fun getWorkoutLogsByDate(date: String): Flow<List<WorkoutLog>> = workoutDao.getWorkoutLogsByDate(date)
    fun getWorkoutLogsBetween(startDate: String, endDate: String): Flow<List<WorkoutLog>> = workoutDao.getWorkoutLogsBetweenDates(startDate, endDate)
    suspend fun insertWorkoutLog(workoutLog: WorkoutLog): Long = workoutDao.insertWorkoutLog(workoutLog)
    suspend fun deleteWorkoutLog(workoutLog: WorkoutLog) = workoutDao.deleteWorkoutLog(workoutLog)

    // Water Logs
    fun getWaterLogsByDate(date: String): Flow<List<WaterLog>> = waterDao.getWaterLogsByDate(date)
    fun getTotalWaterByDate(date: String): Flow<Int?> = waterDao.getTotalWaterByDate(date)
    fun getWaterLogsBetween(startDate: String, endDate: String): Flow<List<WaterLog>> = waterDao.getWaterLogsBetweenDates(startDate, endDate)
    suspend fun insertWaterLog(waterLog: WaterLog): Long = waterDao.insertWaterLog(waterLog)
    suspend fun deleteWaterLog(waterLog: WaterLog) = waterDao.deleteWaterLog(waterLog)

    // Weight Logs
    fun getAllWeightLogs(): Flow<List<WeightLog>> = weightDao.getAllWeightLogs()
    fun getLatestWeightLog(): Flow<WeightLog?> = weightDao.getLatestWeightLog()
    suspend fun getWeightLogForDate(date: String): WeightLog? = weightDao.getWeightLogForDate(date)
    fun getWeightLogsBetween(startDate: String, endDate: String): Flow<List<WeightLog>> = weightDao.getWeightLogsBetweenDates(startDate, endDate)
    suspend fun insertWeightLog(weightLog: WeightLog): Long = weightDao.insertWeightLog(weightLog)
    suspend fun deleteWeightLog(weightLog: WeightLog) = weightDao.deleteWeightLog(weightLog)

    // Coach Messages & Saved Insights
    fun getAllCoachMessages(): Flow<List<CoachMessage>> = coachDao.getAllMessages()
    fun getSavedInsights(): Flow<List<CoachMessage>> = coachDao.getSavedInsights()
    suspend fun insertCoachMessage(message: CoachMessage): Long = coachDao.insertMessage(message)
    suspend fun toggleSaveInsight(id: Long, saved: Boolean, category: String = "Nutrisi", notes: String = "") {
        coachDao.updateInsightStatus(id, saved, category, notes)
    }
    suspend fun deleteCoachMessage(message: CoachMessage) = coachDao.deleteMessage(message)

    // Recipes
    fun getAllRecipes(): Flow<List<CustomRecipe>> = recipeDao.getAllRecipes()
    fun getRecipesByCategory(category: String): Flow<List<CustomRecipe>> = recipeDao.getRecipesByCategory(category)
    fun getFavoriteRecipes(): Flow<List<CustomRecipe>> = recipeDao.getFavoriteRecipes()
    suspend fun insertRecipe(recipe: CustomRecipe): Long = recipeDao.insertRecipe(recipe)
    suspend fun updateRecipe(recipe: CustomRecipe) = recipeDao.updateRecipe(recipe)
    suspend fun deleteRecipe(recipe: CustomRecipe) = recipeDao.deleteRecipe(recipe)

    suspend fun seedInitialDataIfNeeded() {
        if (recipeDao.getRecipeCount() == 0) {
            recipeDao.insertAll(RecipeSeedData.initialRecipes)
        }
    }
}
