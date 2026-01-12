package com.example.casinoapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.casinoapp.data.CasinoDatabaseInstance
import com.example.casinoapp.data.User
import com.example.casinoapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)


        if (!sessionManager.isLoggedIn()) {

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nastavení Toolbaru jako ActionBar
        setSupportActionBar(binding.toolbar)

        val db = CasinoDatabaseInstance.getDatabase(this)
        val userDao = db.userDao()
        val userId = sessionManager.getUserId()

        lifecycleScope.launch {
            userDao.getUserById(userId).collect { user ->
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        currentUser = user
                        binding.tvBalance.text = "${user.balance.toInt()} Kč"
                        binding.tvUserDisplay.text = "Hráč: ${user.username}"
                    } else {
                        // Pokud uživatel v DB není (např. po smazání), odhlas ho
                        sessionManager.logout()
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                        finish()
                    }
                }
            }
        }


        binding.btnAddMoney.setOnClickListener { showDepositDialog() }
        binding.btnPlayRoulette.setOnClickListener {
            startActivity(Intent(this, RouletteActivity::class.java))
        }
        binding.btnPlayDice.setOnClickListener {
            startActivity(Intent(this, DiceActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_login -> {
                showAccountDialog()
                true
            }
            R.id.nav_reset -> {
                confirmReset()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAccountDialog() {
        val options = arrayOf("Odhlásit se", "Resetovat balance", "Zrušit")

        AlertDialog.Builder(this)
            .setTitle("Účet: ${currentUser?.username}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> logout()
                    1 -> confirmReset()
                }
            }
            .show()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Odhlásit se?")
            .setMessage("Opravdu se chceš odhlásit?")
            .setPositiveButton("Ano") { _, _ ->
                sessionManager.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun showDepositDialog() {
        val castky = arrayOf("100 Kč", "500 Kč", "1000 Kč", "5000 Kč")
        AlertDialog.Builder(this)
            .setTitle("Dobít konto")
            .setItems(castky) { _, index ->
                val vklad = when (index) {
                    0 -> 100.0
                    1 -> 500.0
                    2 -> 1000.0
                    3 -> 5000.0
                    else -> 0.0
                }
                updateBalance(vklad)
            }.show()
    }

    private fun updateBalance(amount: Double) {
        currentUser?.let { user ->
            lifecycleScope.launch(Dispatchers.IO) {
                val db = CasinoDatabaseInstance.getDatabase(this@MainActivity)
                db.userDao().updateUser(user.copy(balance = user.balance + amount))
            }
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("Resetovat balance?")
            .setMessage("Balance bude nastaven na 1000 Kč.")
            .setPositiveButton("Ano") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    currentUser?.let { user ->
                        val db = CasinoDatabaseInstance.getDatabase(this@MainActivity)
                        db.userDao().updateUser(user.copy(balance = 1000.0))
                    }
                }
            }
            .setNegativeButton("Ne", null)
            .show()
    }
}