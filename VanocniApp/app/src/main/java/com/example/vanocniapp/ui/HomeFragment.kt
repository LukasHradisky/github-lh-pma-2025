package com.example.vanocniapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vanocniapp.R
import com.example.vanocniapp.UserPreferencesRepository
import com.example.vanocniapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!


    private lateinit var repo: UserPreferencesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        repo = UserPreferencesRepository(requireContext())


        viewLifecycleOwner.lifecycleScope.launch {
            repo.usernameFlow.collectLatest { jmeno ->
                binding.textWelcome.text = if (jmeno.isEmpty()) "Vítej, koledníku!" else "Ahoj, $jmeno!"
            }
        }

        // Logika
        val dnes = java.util.Calendar.getInstance()
        val vanoce = java.util.Calendar.getInstance()
        vanoce.set(java.util.Calendar.MONTH, java.util.Calendar.DECEMBER)
        vanoce.set(java.util.Calendar.DAY_OF_MONTH, 24)

        // Pokud už Vánoce letos byly
        if (dnes.after(vanoce)) {
            vanoce.add(java.util.Calendar.YEAR, 1)
        }

        val rozdil = vanoce.timeInMillis - dnes.timeInMillis
        val dny = rozdil / (24 * 60 * 60 * 1000)

        binding.textDaysUntil.text = "Do Vánoc zbývá $dny dní!"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}