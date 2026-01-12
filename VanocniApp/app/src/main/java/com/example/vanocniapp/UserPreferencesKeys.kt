package com.example.vanocniapp

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferencesKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val MAIN_WISH = stringPreferencesKey("main_wish")
}