package com.example.data.db

import androidx.room.*
import com.example.data.model.CoachMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachMessageDao {
    @Query("SELECT * FROM coach_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<CoachMessage>>

    @Query("SELECT * FROM coach_messages WHERE isSavedAsInsight = 1 ORDER BY timestamp DESC")
    fun getSavedInsights(): Flow<List<CoachMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CoachMessage): Long

    @Update
    suspend fun updateMessage(message: CoachMessage)

    @Query("UPDATE coach_messages SET isSavedAsInsight = :saved, insightCategory = :category, insightNotes = :notes WHERE id = :id")
    suspend fun updateInsightStatus(id: Long, saved: Boolean, category: String, notes: String)

    @Delete
    suspend fun deleteMessage(message: CoachMessage)
}
