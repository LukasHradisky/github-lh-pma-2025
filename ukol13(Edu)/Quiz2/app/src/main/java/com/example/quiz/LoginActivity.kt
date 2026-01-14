package com.example.quiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.QuizDatabase
import com.example.quiz.data.User
import com.example.quiz.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: QuizDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // kontrola
        val userId = getLoggedUserId()
        if (userId != -1) {
            navigateToMenu(userId)
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    private fun handleLogin() {
        val username = binding.etUsername.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(this, "Zadejte uživatelské jméno", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val user = database.quizDao().getUserByUsername(username)

            if (user != null) {
                saveLoggedUserId(user.id)
                navigateToMenu(user.id)
            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Uživatel nenalezen. Zaregistrujte se.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleRegister() {
        val username = binding.etUsername.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(this, "Zadejte uživatelské jméno", Toast.LENGTH_SHORT).show()
            return
        }

        if (username.length < 3) {
            Toast.makeText(this, "Jméno musí mít alespoň 3 znaky", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val existingUser = database.quizDao().getUserByUsername(username)

            if (existingUser != null) {
                Toast.makeText(
                    this@LoginActivity,
                    "Toto jméno již existuje",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val newUser = User(username = username)
                val userId = database.quizDao().insertUser(newUser).toInt()

                saveLoggedUserId(userId)
                Toast.makeText(
                    this@LoginActivity,
                    "Registrace úspěšná! Vítejte!",
                    Toast.LENGTH_SHORT
                ).show()
                navigateToMenu(userId)
            }
        }
    }

    private fun saveLoggedUserId(userId: Int) {
        val prefs = getSharedPreferences("KotlinQuest", Context.MODE_PRIVATE)
        prefs.edit().putInt("logged_user_id", userId).apply()
    }

    private fun getLoggedUserId(): Int {
        val prefs = getSharedPreferences("KotlinQuest", Context.MODE_PRIVATE)
        return prefs.getInt("logged_user_id", -1)
    }

    private fun navigateToMenu(userId: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
        finish()
    }
}