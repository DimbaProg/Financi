package com.example.financi

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey

object PreferencesKeys {
    val USER_ID = longPreferencesKey("user_id")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
}