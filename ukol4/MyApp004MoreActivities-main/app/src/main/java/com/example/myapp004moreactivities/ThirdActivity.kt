package com.example.myapp004moreactivities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp004moreactivities.databinding.ActivityThirdBinding

class ThirdActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThirdBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Třetí aktivita"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val nickname = intent.getStringExtra("USER_NICKNAME") ?: "Neznámý"
        val email = intent.getStringExtra("USER_EMAIL") ?: "Neznámý"
        val age = intent.getStringExtra("USER_AGE") ?: "Neznámý"
        val city = intent.getStringExtra("USER_CITY") ?: "Neznámý"

        binding.twThirdInfo.text = """
            Data z předchozích aktivit:
            
            Přezdívka: $nickname
            Email: $email
            Věk: $age
            Město: $city
            
            Doplňte ještě oslovení:
        """.trimIndent()

        binding.btnSend.setOnClickListener {
            val salutation = binding.etSalutation.text.toString()

            if (salutation.isEmpty()) {
                Toast.makeText(this, "Prosím vyplňte oslovení", Toast.LENGTH_SHORT).show()
            } else {
                val finalMessage = """
                    Odeslané údaje:
                    
                    Oslovení: $salutation
                    Jméno: $nickname
                    Email: $email
                    Věk: $age
                    Město: $city
                    
                    Data byla úspěšně odeslána!
                """.trimIndent()

                Toast.makeText(this, "Data byla úspěšně odeslána!", Toast.LENGTH_LONG).show()

                finishAffinity()
            }
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}