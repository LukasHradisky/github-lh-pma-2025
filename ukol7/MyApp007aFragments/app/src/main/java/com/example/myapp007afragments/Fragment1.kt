package com.example.myapp007afragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast

class Fragment1 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_1, container, false)

        // Najdeme checkboxy a tlačítko
        val cbPizza = view.findViewById<CheckBox>(R.id.cbPizza)
        val cbBurger = view.findViewById<CheckBox>(R.id.cbBurger)
        val cbPasta = view.findViewById<CheckBox>(R.id.cbPasta)
        val cbSalat = view.findViewById<CheckBox>(R.id.cbSalat)
        val btnObjednat = view.findViewById<Button>(R.id.btnObjednat)

        btnObjednat.setOnClickListener {
            val objednavka = mutableListOf<String>()

            if (cbPizza.isChecked) objednavka.add("Pizza")
            if (cbBurger.isChecked) objednavka.add("Burger")
            if (cbPasta.isChecked) objednavka.add("Pasta")
            if (cbSalat.isChecked) objednavka.add("Salát")

            if (objednavka.isEmpty()) {
                Toast.makeText(context, "Vyber si něco z menu!", Toast.LENGTH_SHORT).show()
            } else {
                val text = "Objednáno: ${objednavka.joinToString(", ")}"
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}