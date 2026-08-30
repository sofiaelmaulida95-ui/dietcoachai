package com.example.data.db

import androidx.room.*
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutLogDao {
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getWorkoutLogsByDate(date: String): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getWorkoutLogsBetweenDates(startDate: String, endDate: String): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(workoutLog: WorkoutLog): Long

    @Update
    suspend fun updateWorkoutLog(workoutLog: WorkoutLog)

    @Delete
    suspend fun deleteWorkoutLog(workoutLog: WorkoutLog)

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLogById(id: Long)
}
