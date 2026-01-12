package com.example.vanocniapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.vanocniapp.R
import com.example.vanocniapp.UserPreferencesRepository
import com.example.vanocniapp.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var repo: UserPreferencesRepository

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)
        repo = UserPreferencesRepository(requireContext())

        binding.buttonSaveUsername.setOnClickListener {
            val name = binding.editUsername.text.toString()
            lifecycleScope.launch {
                repo.setUsername(name)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}