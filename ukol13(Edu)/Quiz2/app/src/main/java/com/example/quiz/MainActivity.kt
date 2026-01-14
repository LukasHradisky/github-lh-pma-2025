package com.example.quiz

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.QuizDatabase
import com.example.quiz.data.User
import com.example.quiz.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: QuizDatabase
    private var userId: Int = -1
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)
        userId = intent.getIntExtra("user_id", -1)

        setupButtons()
        loadUserData()
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        checkForActiveSession()
    }

    private fun setupButtons() {
        binding.btnStartNewGame.setOnClickListener {
            startNewGame()
        }

        binding.btnContinueGame.setOnClickListener {
            continueGame()
        }

        binding.btnStatistics.setOnClickListener {
            showStatistics()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            currentUser = database.quizDao().getUserById(userId)
            currentUser?.let { user ->
                binding.tvWelcome.text = "Vítej, ${user.username}!"
                binding.tvTotalScore.text = "Celkové skóre: ${user.totalScore}"
                binding.tvGamesPlayed.text = "Odehráno her: ${user.gamesPlayed}"
                binding.tvCurrentLevel.text = "Level: ${user.currentLevel}"
            }
        }
    }

    private fun checkForActiveSession() {
        lifecycleScope.launch {
            val activeSession = database.quizDao().getActiveSession(userId)
            binding.btnContinueGame.isEnabled = activeSession != null

            if (activeSession != null) {
                binding.tvContinueInfo.text =
                    "Máš rozehranou hru (${activeSession.questionsAnswered}/10)"
            } else {
                binding.tvContinueInfo.text = "Žádná rozehraná hra"
            }
        }
    }

    private fun startNewGame() {
        lifecycleScope.launch {
            val activeSession = database.quizDao().getActiveSession(userId)

            if (activeSession != null) {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Rozehraná hra")
                    .setMessage("Máš rozehranou hru. Chceš ji smazat a začít novou?")
                    .setPositiveButton("Ano") { _, _ ->
                        lifecycleScope.launch {
                            database.quizDao().deleteSession(activeSession.id)
                            launchQuizActivity()
                        }
                    }
                    .setNegativeButton("Ne", null)
                    .show()
            } else {
                launchQuizActivity()
            }
        }
    }

    private fun continueGame() {
        launchQuizActivity()
    }

    private fun launchQuizActivity() {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("user_id", userId)
        startActivity(intent)
    }

    private fun showStatistics() {
        lifecycleScope.launch {
            val accuracy = database.quizDao().getUserAccuracy(userId) ?: 0f

            AlertDialog.Builder(this@MainActivity)
                .setTitle("📊 Tvoje statistiky")
                .setMessage(
                    """
                    Celkové skóre: ${currentUser?.totalScore ?: 0}
                    Počet her: ${currentUser?.gamesPlayed ?: 0}
                    Úspěšnost: ${"%.1f".format(accuracy)}%
                    Level: ${currentUser?.currentLevel ?: 1}
                    """.trimIndent()
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Odhlášení")
            .setMessage("Opravdu se chceš odhlásit?")
            .setPositiveButton("Ano") { _, _ ->
                val prefs = getSharedPreferences("KotlinQuest", Context.MODE_PRIVATE)
                prefs.edit().remove("logged_user_id").apply()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Ne", null)
            .show()
    }
}