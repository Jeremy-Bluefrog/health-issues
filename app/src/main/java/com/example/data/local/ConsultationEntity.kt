package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consultation_history")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val initialSymptom: String,
    val triageLevelName: String,
    val summary: String,
    val possibleCausesSummary: String,
    val recommendedDept: String,
    val fullJson: String
)
