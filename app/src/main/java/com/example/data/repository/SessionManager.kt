package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "partino_session")

class SessionManager(private val context: Context) {

    companion object {
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_LANGUAGE = stringPreferencesKey("language") // "fa" or "en"
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    val username: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USERNAME] ?: ""
    }

    val displayName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_DISPLAY_NAME] ?: ""
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "fa"
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DARK_MODE] ?: true
    }

    suspend fun saveSession(username: String, displayName: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_USERNAME] = username
            preferences[KEY_DISPLAY_NAME] = displayName
        }
    }

    suspend fun updateLanguage(lang: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = lang
        }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DARK_MODE] = isDark
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences[KEY_USERNAME] = ""
            preferences[KEY_DISPLAY_NAME] = ""
        }
    }
}
