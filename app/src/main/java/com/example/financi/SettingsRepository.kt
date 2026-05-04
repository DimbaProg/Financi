package com.example.financi

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val userId: Flow<Long> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_ID] ?: -1L
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DARK_THEME] ?: false
    }

    suspend fun saveUserId(id: Long) {
        dataStore.edit { it[PreferencesKeys.USER_ID] = id }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DARK_THEME] = enabled }
    }

    suspend fun clearSession() {
        dataStore.edit { it.remove(PreferencesKeys.USER_ID) }
    }
}