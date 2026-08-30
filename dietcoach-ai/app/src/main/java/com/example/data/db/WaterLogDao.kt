package com.example.data.db

import androidx.room.*
import com.example.data.model.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {
    @Query("SELECT * FROM water_logs WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterLogsByDate(date: String): Flow<List<WaterLog>>

    @Query("SELECT SUM(amountMl) FROM water_logs WHERE date = :date")
    fun getTotalWaterByDate(date: String): Flow<Int?>

    @Query("SELECT * FROM water_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWaterLogsBetweenDates(startDate: String, endDate: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLog): Long

    @Delete
    suspend fun deleteWaterLog(waterLog: WaterLog)

    @Query("DELETE FROM water_logs WHERE date = :date")
    suspend fun clearWaterLogsForDate(date: String)
}
