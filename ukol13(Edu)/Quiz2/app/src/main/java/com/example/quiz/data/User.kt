package com.example.quiz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val totalScore: Int = 0,
    val gamesPlayed: Int = 0,
    val currentLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)