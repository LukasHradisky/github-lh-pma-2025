package com.example.vanocniapp // TADY MUSÍ BÝT TVŮJ NÁZEV PROJEKTU

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(private val context: Context) {


    private val dataStore = context.settingsDataStore

    // Načítání jména
    val usernameFlow: Flow<String> = dataStore.data.map { prefs ->

        prefs[UserPreferencesKeys.USER_NAME] ?: ""
    }

    // uložení jména
    suspend fun setUsername(name: String) {
        dataStore.edit { prefs ->
            prefs[UserPreferencesKeys.USER_NAME] = name
        }
    }
}