package com.example.quiz


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.QuizDatabase
import com.example.quiz.databinding.ActivityResultBinding
import kotlinx.coroutines.launch

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var database: QuizDatabase
    private var sessionId: Int = -1
    private var userId: Int = -1
    private var score: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)

        sessionId = intent.getIntExtra("session_id", -1)
        userId = intent.getIntExtra("user_id", -1)
        score = intent.getIntExtra("score", 0)

        displayResults()

        binding.btnBackToMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        }

        binding.btnPlayAgain.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            finish()
        }
    }

    private fun displayResults() {
        lifecycleScope.launch {
            val correctCount = database.quizDao().getCorrectAnswersCount(sessionId)
            val totalQuestions = 10
            val accuracy = (correctCount.toFloat() / totalQuestions * 100).toInt()

            binding.tvFinalScore.text = "$score bodů"
            binding.tvCorrectAnswers.text = "$correctCount/$totalQuestions správně"
            binding.tvAccuracy.text = "Úspěšnost: $accuracy%"


            val rating = when {
                accuracy >= 90 -> "🏆 Perfektní! Jsi Kotlin mistr!"
                accuracy >= 70 -> "🌟 Skvělé! Jsi na dobré cestě!"
                accuracy >= 50 -> "👍 Dobré! Pokračuj v učení!"
                else -> "💪 Zkus to znovu, příště to půjde lépe!"
            }
            binding.tvRating.text = rating


            val user = database.quizDao().getUserById(userId)
            user?.let {
                binding.tvNewLevel.text = "Tvůj level: ${it.currentLevel}"
            }
        }
    }
}