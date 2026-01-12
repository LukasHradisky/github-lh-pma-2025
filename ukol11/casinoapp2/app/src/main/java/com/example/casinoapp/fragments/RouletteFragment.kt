package com.example.casinoapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.casinoapp.data.FirebaseDataManager // Používáme FirebaseDataManager
import com.example.casinoapp.databinding.FragmentRouletteBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.random.Random


class RouletteFragment : Fragment() {

    private var _binding: FragmentRouletteBinding? = null
    private val binding get() = _binding!!
    private lateinit var dataManager: FirebaseDataManager // Změna typu na FirebaseDataManager

    // Posluchač pro aktualizaci kreditu v reálném čase z Firebase
    private val balanceListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Přečte aktuální kredit z databáze, pokud neexistuje, použije 0
            val balance = snapshot.getValue(Int::class.java) ?: 0
            updateBalanceText(balance)
        }

        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(requireContext(), "Chyba při načítání kreditu: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouletteBinding.inflate(inflater, container, false)
        dataManager = FirebaseDataManager() // Inicializace
        dataManager.initializeBalance() // Zajistí, že je v databázi počáteční kredit (1000 Kč)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonSpin.setOnClickListener {
            val betText = binding.editBet.text.toString()
            val betAmount = betText.toIntOrNull()

            // 1. Základní validace sázky
            if (betAmount == null || betAmount <= 0) {
                Toast.makeText(requireContext(), "Zadej platnou sázku (kladné číslo)!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val numberInput = binding.editNumber.text.toString()
            val colorId = binding.radioGroupColor.checkedRadioButtonId

            // 2. Rozhodnutí o typu sázky a validace vstupu
            val betType: String
            var chosenNumber: Int? = null
            var chosenColor: String? = null

            if (numberInput.isNotEmpty()) {
                // Uživatel vsází na číslo
                val number = numberInput.toIntOrNull()
                if (number == null || number !in 0..36) {
                    Toast.makeText(requireContext(), "Číslo musí být 0–36", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                betType = "number"
                chosenNumber = number

                // Zrušíme výběr barvy, protože číslo má prioritu
                binding.radioGroupColor.clearCheck()

            } else if (colorId != -1) {
                // Uživatel vsází na barvu (pole čísla je prázdné)
                betType = "color"
                val radioButton = view.findViewById<RadioButton>(colorId)
                chosenColor = radioButton?.text.toString()

            } else {
                // Nebylo vsazeno ani na barvu, ani na číslo
                Toast.makeText(requireContext(), "Vyber barvu NEBO zadej číslo!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Asynchronní kontrola kreditu a provedení sázky
            dataManager.getBalance { currentBalance ->
                if (betAmount > currentBalance) {
                    Toast.makeText(requireContext(), "Nemáš dostatek kreditu! (Aktuálně: $currentBalance Kč)", Toast.LENGTH_SHORT).show()
                    return@getBalance // Stop pokud nemá kredit
                }

                // Kredit je dostatečný, spouštíme ruletu
                spinRoulette(betType, chosenNumber, chosenColor, betAmount, currentBalance)
            }
        }
    }

    // Přidáme poslech změn kreditu při zobrazení fragmentu
    override fun onResume() {
        super.onResume()
        dataManager.listenToBalanceChanges(balanceListener)
    }

    // Odstraníme poslech při skrytí fragmentu, abychom šetřili zdroje
    override fun onPause() {
        super.onPause()
        dataManager.removeBalanceListener(balanceListener)
    }

    private fun spinRoulette(betType: String, chosenNumber: Int?, chosenColor: String?, betAmount: Int, initialBalance: Int) {
        // Logika rulety
        val result = Random.nextInt(0, 37) // 0 až 36
        val resultColor = when {
            result == 0 -> "Zelená"
            result % 2 == 0 -> "Černá"
            else -> "Červená"
        }

        var newBalance = initialBalance
        var message = "Padlo číslo $result ($resultColor). "
        var won = false
        var multiplier = 0

        // Vyhodnocení sázky
        when (betType) {
            "number" -> {
                if (chosenNumber == result) {
                    multiplier = 36
                    won = true
                }
            }
            "color" -> {
                if (chosenColor == resultColor) {
                    multiplier = 2
                    won = true
                }
            }
        }

        // Aktualizace kreditu a zpráva
        if (won) {
            // Výhra = sázka * (násobitel - 1)
            val winAmount = betAmount * (multiplier - 1)
            newBalance += winAmount
            message += " Vyhráváš $winAmount Kč! (x$multiplier)"
        } else {
            newBalance -= betAmount
            message += " Prohráváš $betAmount Kč."
        }

        dataManager.setBalance(newBalance) // Uložení nového kreditu do Firebase

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }


    private fun updateBalanceText(balance: Int) {
        binding.textBalance.text = " Kredit: $balance Kč"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}