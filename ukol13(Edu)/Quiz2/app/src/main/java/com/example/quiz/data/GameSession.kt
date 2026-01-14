package com.example.quiz.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GameSession(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val questionsAnswered: Int = 0,
    val isCompleted: Boolean = false,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)