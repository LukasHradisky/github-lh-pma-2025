package com.example.casinoapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Vytvoření nového uživatele (registrace)
    @Insert
    suspend fun insertUser(user: User)

    // Načtení uživatele podle jména a hesla (přihlášení)
    @Query("SELECT * FROM user_table WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    // Načtení uživatele podle ID
    @Query("SELECT * FROM user_table WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>

    // Synchronní načtení uživatele podle ID
    @Query("SELECT * FROM user_table WHERE id = :userId")
    suspend fun getUserByIdSync(userId: Int): User?

    // Kontrola, zda uživatel existuje
    @Query("SELECT COUNT(*) FROM user_table WHERE username = :username")
    suspend fun userExists(username: String): Int

    // Aktualizace uživatele
    @Update
    suspend fun updateUser(user: User)

    // Smazání všech uživatelů (pro testování)
    @Query("DELETE FROM user_table")
    suspend fun deleteAllUsers()
}