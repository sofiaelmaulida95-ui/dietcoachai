package com.example.data.db

import androidx.room.*
import com.example.data.model.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {
    @Query("SELECT * FROM weight_logs ORDER BY date ASC, timestamp ASC")
    fun getAllWeightLogs(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs ORDER BY date DESC, timestamp DESC LIMIT 1")
    fun getLatestWeightLog(): Flow<WeightLog?>

    @Query("SELECT * FROM weight_logs WHERE date = :date LIMIT 1")
    suspend fun getWeightLogForDate(date: String): WeightLog?

    @Query("SELECT * FROM weight_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWeightLogsBetweenDates(startDate: String, endDate: String): Flow<List<WeightLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weightLog: WeightLog): Long

    @Update
    suspend fun updateWeightLog(weightLog: WeightLog)

    @Delete
    suspend fun deleteWeightLog(weightLog: WeightLog)
}
