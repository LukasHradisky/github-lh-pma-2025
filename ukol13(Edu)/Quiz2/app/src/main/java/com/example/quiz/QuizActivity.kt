package com.example.quiz

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.quiz.data.*
import com.example.quiz.databinding.ActivityQuizBinding
import kotlinx.coroutines.launch

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    private lateinit var database: QuizDatabase
    private var userId: Int = -1

    private var questions: List<Question> = emptyList()
    private var currentQuestionIndex = 0
    private var score = 0
    private var sessionId: Int = -1

    private var questionStartTime: Long = 0
    private var timer: CountDownTimer? = null
    private val QUESTION_TIME_LIMIT = 30000L // 30 sekund

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = QuizDatabase.getDatabase(this)
        userId = intent.getIntExtra("user_id", -1)

        setupAnswerButtons()
        loadOrCreateSession()
    }

    private fun setupAnswerButtons() {
        binding.btnOptionA.setOnClickListener { checkAnswer("A") }
        binding.btnOptionB.setOnClickListener { checkAnswer("B") }
        binding.btnOptionC.setOnClickListener { checkAnswer("C") }
        binding.btnOptionD.setOnClickListener { checkAnswer("D") }

        binding.btnQuit.setOnClickListener {
            showQuitDialog()
        }
    }

    private fun loadOrCreateSession() {
        lifecycleScope.launch {
            var session = database.quizDao().getActiveSession(userId)

            if (session == null) {
                // Vytvoř novou session
                questions = database.quizDao().getRandomQuestions(10)

                if (questions.isEmpty()) {
                    AlertDialog.Builder(this@QuizActivity)
                        .setTitle("Chyba")
                        .setMessage("V databázi nejsou žádné otázky!")
                        .setPositiveButton("OK") { _, _ -> finish() }
                        .show()
                    return@launch
                }

                val newSession = GameSession(userId = userId)
                sessionId = database.quizDao().insertGameSession(newSession).toInt()
                currentQuestionIndex = 0
                score = 0
            } else {
                // Načti existující session
                sessionId = session.id
                currentQuestionIndex = session.currentQuestionIndex
                score = session.score

                questions = database.quizDao().getRandomQuestions(10)
            }

            displayQuestion()
        }
    }

    private fun displayQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishQuiz()
            return
        }

        val question = questions[currentQuestionIndex]
        questionStartTime = System.currentTimeMillis()

        // Aktualizuj UI
        binding.tvQuestionNumber.text = "Otázka ${currentQuestionIndex + 1}/10"
        binding.tvScore.text = "Skóre: $score"
        binding.tvQuestionText.text = question.questionText
        binding.tvCategory.text = "📚 ${question.category}"

        binding.btnOptionA.text = question.optionA
        binding.btnOptionB.text = question.optionB
        binding.btnOptionC.text = question.optionC
        binding.btnOptionD.text = question.optionD

        // Nastav obtížnost
        val difficultyText = when (question.difficulty) {
            1 -> "⭐ Lehká"
            2 -> "⭐⭐ Střední"
            3 -> "⭐⭐⭐ Těžká"
            else -> "⭐"
        }
        binding.tvDifficulty.text = difficultyText


        resetButtonColors()
        enableButtons(true)


        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()

        timer = object : CountDownTimer(QUESTION_TIME_LIMIT, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.tvTimer.text = "⏱️ $secondsLeft s"

                // Změň barvu při nízkém čase
                if (secondsLeft <= 10) {
                    binding.tvTimer.setTextColor(Color.RED)
                } else {
                    binding.tvTimer.setTextColor(Color.BLACK)
                }
            }

            override fun onFinish() {
                binding.tvTimer.text = "⏱️ 0 s"
                handleTimeout()
            }
        }.start()
    }

    private fun handleTimeout() {
        enableButtons(false)

        // Uloží špatnou odpověď
        val question = questions[currentQuestionIndex]
        lifecycleScope.launch {
            val answer = UserAnswer(
                sessionId = sessionId,
                questionId = question.id,
                userAnswer = "",
                isCorrect = false,
                timeSpentSeconds = 30
            )
            database.quizDao().insertUserAnswer(answer)

            // Zobraz správnou odpověď
            highlightCorrectAnswer()

            binding.tvFeedback.text = "⏰ Čas vypršel! Správná odpověď: ${question.correctAnswer}"
            binding.tvFeedback.visibility = View.VISIBLE

            // Po 2 sekundách další otázka
            binding.root.postDelayed({
                moveToNextQuestion()
            }, 2000)
        }
    }

    private fun checkAnswer(selectedAnswer: String) {
        timer?.cancel()
        enableButtons(false)

        val question = questions[currentQuestionIndex]
        val isCorrect = selectedAnswer == question.correctAnswer
        val timeSpent = ((System.currentTimeMillis() - questionStartTime) / 1000).toInt()

        // Vypočti body (rychlost bonifikuje)
        val points = if (isCorrect) {
            val basePoints = when (question.difficulty) {
                1 -> 10
                2 -> 20
                3 -> 30
                else -> 10
            }
            val timeBonus = maxOf(0, 30 - timeSpent) // Bonus za rychlost
            basePoints + timeBonus
        } else {
            0
        }

        if (isCorrect) {
            score += points
        }

        // Ulož odpověď
        lifecycleScope.launch {
            val answer = UserAnswer(
                sessionId = sessionId,
                questionId = question.id,
                userAnswer = selectedAnswer,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent
            )
            database.quizDao().insertUserAnswer(answer)

            // Aktualizuj session
            val session = GameSession(
                id = sessionId,
                userId = userId,
                currentQuestionIndex = currentQuestionIndex + 1,
                score = score,
                questionsAnswered = currentQuestionIndex + 1
            )
            database.quizDao().updateGameSession(session)
        }

        // Zobraz feedback
        highlightAnswer(selectedAnswer, isCorrect)

        val feedbackText = if (isCorrect) {
            "✅ Správně! +$points bodů"
        } else {
            "❌ Špatně! Správná odpověď: ${question.correctAnswer}"
        }

        binding.tvFeedback.text = feedbackText
        binding.tvFeedback.visibility = View.VISIBLE
        binding.tvScore.text = "Skóre: $score"

        // Po 2 sekundách další otázka
        binding.root.postDelayed({
            moveToNextQuestion()
        }, 2000)
    }

    private fun highlightAnswer(selectedAnswer: String, isCorrect: Boolean) {
        val selectedButton = when (selectedAnswer) {
            "A" -> binding.btnOptionA
            "B" -> binding.btnOptionB
            "C" -> binding.btnOptionC
            "D" -> binding.btnOptionD
            else -> null
        }

        val color = if (isCorrect) Color.GREEN else Color.RED
        selectedButton?.setBackgroundColor(color)

        if (!isCorrect) {
            highlightCorrectAnswer()
        }
    }

    private fun highlightCorrectAnswer() {
        val correctAnswer = questions[currentQuestionIndex].correctAnswer
        val correctButton = when (correctAnswer) {
            "A" -> binding.btnOptionA
            "B" -> binding.btnOptionB
            "C" -> binding.btnOptionC
            "D" -> binding.btnOptionD
            else -> null
        }
        correctButton?.setBackgroundColor(Color.GREEN)
    }

    private fun moveToNextQuestion() {
        currentQuestionIndex++
        binding.tvFeedback.visibility = View.GONE
        displayQuestion()
    }

    private fun finishQuiz() {
        timer?.cancel()

        lifecycleScope.launch {
            // Označ session jako dokončenou
            val session = GameSession(
                id = sessionId,
                userId = userId,
                currentQuestionIndex = currentQuestionIndex,
                score = score,
                questionsAnswered = questions.size,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            database.quizDao().updateGameSession(session)

            // Aktualizuj uživatele
            val user = database.quizDao().getUserById(userId)
            if (user != null) {
                val updatedUser = user.copy(
                    totalScore = user.totalScore + score,
                    gamesPlayed = user.gamesPlayed + 1,
                    currentLevel = (user.totalScore + score) / 100 + 1
                )
                database.quizDao().updateUser(updatedUser)
            }

            // Přejdi na výsledky
            val intent = Intent(this@QuizActivity, ResultActivity::class.java)
            intent.putExtra("session_id", sessionId)
            intent.putExtra("user_id", userId)
            intent.putExtra("score", score)
            startActivity(intent)
            finish()
        }
    }

    private fun showQuitDialog() {
        AlertDialog.Builder(this)
            .setTitle("Ukončit kvíz?")
            .setMessage("Tvůj progress bude uložen a můžeš pokračovat později.")
            .setPositiveButton("Ano") { _, _ ->
                finish()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun resetButtonColors() {
        val defaultColor = Color.parseColor("#E0E0E0")
        binding.btnOptionA.setBackgroundColor(defaultColor)
        binding.btnOptionB.setBackgroundColor(defaultColor)
        binding.btnOptionC.setBackgroundColor(defaultColor)
        binding.btnOptionD.setBackgroundColor(defaultColor)
    }

    private fun enableButtons(enabled: Boolean) {
        binding.btnOptionA.isEnabled = enabled
        binding.btnOptionB.isEnabled = enabled
        binding.btnOptionC.isEnabled = enabled
        binding.btnOptionD.isEnabled = enabled
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}