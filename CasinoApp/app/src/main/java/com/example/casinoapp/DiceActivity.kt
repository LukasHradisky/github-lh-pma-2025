package com.example.casinoapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.casinoapp.data.CasinoDatabaseInstance
import com.example.casinoapp.data.User
import com.example.casinoapp.databinding.ActivityDiceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiceBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Nastavení Toolbaru
        setSupportActionBar(binding.toolbarDice)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarDice.setNavigationOnClickListener {
            finish()
        }

        val db = CasinoDatabaseInstance.getDatabase(this)
        val userDao = db.userDao()
        val userId = sessionManager.getUserId()

        // Sledování balance
        lifecycleScope.launch {
            userDao.getUserById(userId).collect { user ->
                currentUser = user
                binding.tvDiceBalance.text = "Konto: ${user?.balance?.toInt()} Kč"
            }
        }

        // Tlačítko pro hod kostkami
        binding.btnRollDice.setOnClickListener {
            hratKostky()
        }
    }

    private fun hratKostky() {
        val sazka = binding.etDiceBet.text.toString().toDoubleOrNull() ?: 0.0
        val balance = currentUser?.balance ?: 0.0

        if (sazka <= 0.0) {
            Toast.makeText(this, "Zadej platnou sázku!", Toast.LENGTH_SHORT).show()
            return
        }

        if (sazka > balance) {
            Toast.makeText(this, "Nemáš dostatek peněz!", Toast.LENGTH_SHORT).show()
            return
        }

        // Animace hodu
        lifecycleScope.launch {
            binding.btnRollDice.isEnabled = false

            // Simulace házení
            repeat(10) {
                binding.tvDice1.text = (1..6).random().toString()
                binding.tvDice2.text = (1..6).random().toString()
                delay(100)
            }

            // Finální hod
            val dice1 = (1..6).random()
            val dice2 = (1..6).random()
            val suma = dice1 + dice2

            binding.tvDice1.text = dice1.toString()
            binding.tvDice2.text = dice2.toString()
            binding.tvDiceSum.text = "Součet: $suma"

            // Vyhodnocení
            val vysledek = when (suma) {
                7, 11 -> {
                    binding.tvDiceResult.text = "VÝHRA! +${(sazka * 2).toInt()} Kč"
                    binding.tvDiceResult.setTextColor(Color.GREEN)
                    sazka * 2
                }
                2, 3, 12 -> {
                    binding.tvDiceResult.text = "PROHRA! -${sazka.toInt()} Kč"
                    binding.tvDiceResult.setTextColor(Color.RED)
                    -sazka
                }
                else -> {
                    binding.tvDiceResult.text = "Neutrální hod"
                    binding.tvDiceResult.setTextColor(Color.YELLOW)
                    0.0
                }
            }

            // Aktualizace balance
            if (vysledek != 0.0) {
                lifecycleScope.launch(Dispatchers.IO) {
                    currentUser?.let {
                        CasinoDatabaseInstance.getDatabase(this@DiceActivity).userDao()
                            .updateUser(it.copy(balance = it.balance + vysledek))
                    }
                }
            }

            binding.btnRollDice.isEnabled = true
        }
    }
}