package com.example.casinoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.casinoapp.databinding.ActivityGameBinding
import com.example.casinoapp.fragments.RouletteFragment


class GameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, RouletteFragment())
            .commit()
    }
}
