package com.example.myapp007afragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast

class Fragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_2, container, false)

        // Najdeme checkboxy a tlačítko
        val cbKola = view.findViewById<CheckBox>(R.id.cbKola)
        val cbLimo = view.findViewById<CheckBox>(R.id.cbLimo)
        val cbPivo = view.findViewById<CheckBox>(R.id.cbPivo)
        val cbVino = view.findViewById<CheckBox>(R.id.cbVino)
        val btnObjednatNapoje = view.findViewById<Button>(R.id.btnObjednatNapoje)

        btnObjednatNapoje.setOnClickListener {
            val objednavka = mutableListOf<String>()

            if (cbKola.isChecked) objednavka.add("Kola")
            if (cbLimo.isChecked) objednavka.add("Limonáda")
            if (cbPivo.isChecked) objednavka.add("Pivo")
            if (cbVino.isChecked) objednavka.add("Víno")

            if (objednavka.isEmpty()) {
                Toast.makeText(context, "Vyber si nějaký nápoj!", Toast.LENGTH_SHORT).show()
            } else {
                val text = "Objednáno: ${objednavka.joinToString(", ")}"
                Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            }
        }

        return view
    }
}