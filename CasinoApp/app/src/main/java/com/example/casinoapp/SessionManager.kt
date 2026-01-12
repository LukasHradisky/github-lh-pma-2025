package com.example.casinoapp

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("casino_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    // Uložení přihlášeného uživatele
    fun saveLogin(userId: Int) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    // Získání ID přihlášeného uživatele
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }

    // Kontrola, zda je někdo přihlášen
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Odhlášení
    fun logout() {
        prefs.edit().apply {
            clear()
            apply()
        }
    }
}