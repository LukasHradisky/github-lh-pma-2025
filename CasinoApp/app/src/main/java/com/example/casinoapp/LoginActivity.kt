package com.example.casinoapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.casinoapp.data.CasinoDatabaseInstance
import com.example.casinoapp.data.User
import com.example.casinoapp.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Nastavení Toolbaru
        setSupportActionBar(binding.toolbarLogin)

        // Tlačítko přihlášení
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vyplň všechna pole!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(username, password)
        }

        // Tlačítko registrace
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vyplň všechna pole!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 4) {
                Toast.makeText(this, "Heslo musí mít alespoň 4 znaky!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(username, password)
        }

        // Host přihlášení
        binding.tvGuestLogin.setOnClickListener {
            createGuestAccount()
        }
    }

    private fun loginUser(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = CasinoDatabaseInstance.getDatabase(this@LoginActivity).userDao()
            val user = dao.login(username, password)

            withContext(Dispatchers.Main) {
                if (user != null) {
                    sessionManager.saveLogin(user.id)
                    Toast.makeText(this@LoginActivity, "Vítej zpět, ${user.username}!", Toast.LENGTH_SHORT).show()

                    // Přejdi na MainActivity
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Špatné jméno nebo heslo!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUser(username: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = CasinoDatabaseInstance.getDatabase(this@LoginActivity).userDao()
            val exists = dao.userExists(username) > 0

            if (exists) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Toto jméno už existuje!", Toast.LENGTH_SHORT).show()
                }
            } else {

                val newUser = User(username = username, password = password, balance = 1000.0)
                dao.insertUser(newUser)


                val createdUser = dao.login(username, password)

                withContext(Dispatchers.Main) {
                    if (createdUser != null) {
                        sessionManager.saveLogin(createdUser.id)
                        Toast.makeText(this@LoginActivity, "Účet vytvořen!", Toast.LENGTH_LONG).show()

                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Ukončí login, aby se uživatel nemohl vrátit zpět tlačítkem
                    }
                }
            }
        }
    }


    private fun createGuestAccount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = CasinoDatabaseInstance.getDatabase(this@LoginActivity).userDao()

            // Vytvoř unikátní host účet
            val guestName = "Host_${System.currentTimeMillis()}"
            val guestUser = User(username = guestName, password = "guest", balance = 1000.0)
            dao.insertUser(guestUser)

            val createdUser = dao.login(guestName, "guest")

            withContext(Dispatchers.Main) {
                if (createdUser != null) {
                    sessionManager.saveLogin(createdUser.id)
                    Toast.makeText(this@LoginActivity, "Hraješ jako host!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}