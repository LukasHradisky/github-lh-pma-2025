package com.example.quiz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String, // "A", "B", "C", or "D"
    val difficulty: Int = 1, // 1 = Easy, 2 = Medium, 3 = Hard
    val category: String = "Kotlin" // "Kotlin", "Android", "Room", etc.
)