package com.example.casinoapp

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.casinoapp.data.CasinoDatabaseInstance
import com.example.casinoapp.data.User
import com.example.casinoapp.databinding.ActivityRouletteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RouletteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRouletteBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouletteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Nastavení Toolbaru
        setSupportActionBar(binding.toolbarRoulette)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarRoulette.setNavigationOnClickListener {
            finish()
        }

        val db = CasinoDatabaseInstance.getDatabase(this)
        val userDao = db.userDao()
        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            userDao.getUserById(userId).collect { user ->
                currentUser = user
                binding.tvRouletteBalance.text = "Konto: ${user?.balance?.toInt()} Kč"
            }
        }

        binding.btnBetRed.setOnClickListener { hrajBarvu(true) }
        binding.btnBetBlack.setOnClickListener { hrajBarvu(false) }
        binding.btnBetNumber.setOnClickListener { hrajCislo() }
    }

    private fun hrajCislo() {
        val sazka = binding.etBetAmount.text.toString().toDoubleOrNull() ?: 0.0
        val vsazeneCislo = binding.etBetNumber.text.toString().toIntOrNull()

        if (vsazeneCislo == null || vsazeneCislo !in 0..36 || sazka <= 0.0) {
            Toast.makeText(this, "Neplatná sázka nebo číslo!", Toast.LENGTH_SHORT).show()
            return
        }

        provestHru(sazka) { vylosovane -> vylosovane == vsazeneCislo }
    }

    private fun hrajBarvu(vsazenaCervena: Boolean) {
        val sazka = binding.etBetAmount.text.toString().toDoubleOrNull() ?: 0.0
        if (sazka <= 0.0) {
            Toast.makeText(this, "Zadej platnou sázku!", Toast.LENGTH_SHORT).show()
            return
        }

        val cervenaCisla = setOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
        provestHru(sazka) { vylosovane ->
            vylosovane != 0 && cervenaCisla.contains(vylosovane) == vsazenaCervena
        }
    }

    private fun provestHru(sazka: Double, kontrolaVyhry: (Int) -> Boolean) {
        val balance = currentUser?.balance ?: 0.0
        if (sazka > balance) {
            Toast.makeText(this, "Nemáš dostatek peněz!", Toast.LENGTH_SHORT).show()
            return
        }

        val vylosovane = (0..36).random()
        binding.tvResultNumber.text = vylosovane.toString()

        val vyhra = if (kontrolaVyhry(vylosovane)) {
            if (binding.etBetNumber.text.isNotEmpty()) sazka * 35 else sazka
        } else {
            -sazka
        }

        lifecycleScope.launch(Dispatchers.IO) {
            currentUser?.let {
                CasinoDatabaseInstance.getDatabase(this@RouletteActivity).userDao()
                    .updateUser(it.copy(balance = it.balance + vyhra))
            }
        }

        binding.tvGameStatus.text = if (vyhra > 0) "VÝHRA: ${vyhra.toInt()} Kč" else "PROHRA: ${(-vyhra).toInt()} Kč"
        binding.tvGameStatus.setTextColor(if (vyhra > 0) Color.GREEN else Color.RED)
    }
}