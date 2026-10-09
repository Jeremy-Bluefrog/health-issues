package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultation_history ORDER BY createdAt DESC")
    fun getAllConsultations(): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultation_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ConsultationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConsultationEntity): Long

    @Query("DELETE FROM consultation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM consultation_history")
    suspend fun clearAll()
}
