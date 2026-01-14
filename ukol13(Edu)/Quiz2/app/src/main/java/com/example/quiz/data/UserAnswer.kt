package com.example.quiz.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_answers",
    foreignKeys = [
        ForeignKey(
            entity = GameSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserAnswer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val sessionId: Int,
    val questionId: Int,
    val userAnswer: String, // "A", "B", "C", or "D"
    val isCorrect: Boolean,
    val timeSpentSeconds: Int,
    val answeredAt: Long = System.currentTimeMillis()
)