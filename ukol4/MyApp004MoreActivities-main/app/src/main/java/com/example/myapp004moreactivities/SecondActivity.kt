package com.example.myapp004moreactivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp004moreactivities.databinding.ActivitySecondBinding

class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Druhá aktivita"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val nickname = intent.getStringExtra("NICK_NAME") ?: "Nevyplněno"
        val email = intent.getStringExtra("EMAIL") ?: "Nevyplněno"
        val age = intent.getStringExtra("AGE") ?: "Nevyplněno"
        val city = intent.getStringExtra("CITY") ?: "Nevyplněno"

        binding.twInfo.text = """
            Data z první aktivity:
        
            Přezdívka: $nickname
            Email: $email
            Věk: $age
            Město: $city
        """.trimIndent()

        binding.btnThirdAct.setOnClickListener {

            if (nickname.isEmpty() || nickname == "Nevyplněno") {
                binding.twInfo.text = "Chyba: Nejsou k dispozici žádná data. Vraťte se na hlavní aktivitu."
                return@setOnClickListener
            }

            val intent = Intent(this, ThirdActivity::class.java)
            intent.putExtra("USER_NICKNAME", nickname)
            intent.putExtra("USER_EMAIL", email)
            intent.putExtra("USER_AGE", age)
            intent.putExtra("USER_CITY", city)
            startActivity(intent)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}