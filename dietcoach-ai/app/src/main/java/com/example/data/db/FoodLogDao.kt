package com.example.data.db

import androidx.room.*
import com.example.data.model.FoodLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getFoodLogsByDate(date: String): Flow<List<FoodLog>>

    @Query("SELECT * FROM food_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getFoodLogsBetweenDates(startDate: String, endDate: String): Flow<List<FoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog): Long

    @Update
    suspend fun updateFoodLog(foodLog: FoodLog)

    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLog)

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Long)
}
