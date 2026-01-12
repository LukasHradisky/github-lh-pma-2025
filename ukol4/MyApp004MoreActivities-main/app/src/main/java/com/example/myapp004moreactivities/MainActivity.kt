package com.example.myapp004moreactivities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapp004moreactivities.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Hlavní aktivita"

        binding.btnSecondAct.setOnClickListener {
            val nickname = binding.etNickname.text.toString()
            val email = binding.etEmail.text.toString()
            val age = binding.etAge.text.toString()
            val city = binding.etCity.text.toString()

            val intent = Intent(this, SecondActivity::class.java)
            intent.putExtra("NICK_NAME", nickname)
            intent.putExtra("EMAIL", email)
            intent.putExtra("AGE", age)
            intent.putExtra("CITY", city)
            startActivity(intent)
        }
    }
}