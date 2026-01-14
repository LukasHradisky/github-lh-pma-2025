package com.example.quiz.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    // === USER OPERATIONS ===
    @Insert
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users ORDER BY totalScore DESC LIMIT 10")
    fun getTopUsers(): Flow<List<User>>

    // === QUESTION OPERATIONS ===
    @Insert
    suspend fun insertQuestion(question: Question)

    @Insert
    suspend fun insertQuestions(questions: List<Question>)

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestionsByDifficulty(difficulty: Int, limit: Int): List<Question>

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuestions(limit: Int): List<Question>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    // === GAME SESSION OPERATIONS ===
    @Insert
    suspend fun insertGameSession(session: GameSession): Long

    @Update
    suspend fun updateGameSession(session: GameSession)

    @Query("SELECT * FROM game_sessions WHERE userId = :userId AND isCompleted = 0 LIMIT 1")
    suspend fun getActiveSession(userId: Int): GameSession?

    @Query("SELECT * FROM game_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getUserSessions(userId: Int): Flow<List<GameSession>>

    @Query("DELETE FROM game_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Int)

    // === USER ANSWER OPERATIONS ===
    @Insert
    suspend fun insertUserAnswer(answer: UserAnswer)

    @Query("SELECT * FROM user_answers WHERE sessionId = :sessionId")
    suspend fun getAnswersForSession(sessionId: Int): List<UserAnswer>

    @Query("""
        SELECT COUNT(*) FROM user_answers 
        WHERE sessionId = :sessionId AND isCorrect = 1
    """)
    suspend fun getCorrectAnswersCount(sessionId: Int): Int

    // === STATISTICS ===
    @Query("""
        SELECT AVG(CAST(isCorrect AS FLOAT)) * 100 
        FROM user_answers ua
        JOIN game_sessions gs ON ua.sessionId = gs.id
        WHERE gs.userId = :userId
    """)
    suspend fun getUserAccuracy(userId: Int): Float?
}