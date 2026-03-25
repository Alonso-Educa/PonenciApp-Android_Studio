package com.example.ponenciapp.screens.utilidad

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

// DataStore (extensión del Context)
val Context.dataStore by preferencesDataStore(name = "settings")

// Clave del tema
object ThemePreferences {
    val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")
}

// ViewModel
class ThemeViewModel(context: Context) : ViewModel() {

    private val dataStore = context.dataStore

    var isDarkTheme by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                isDarkTheme = prefs[ThemePreferences.DARK_THEME_KEY] ?: false
            }
        }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[ThemePreferences.DARK_THEME_KEY] = enabled
            }
        }
    }
}

class ThemeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            return ThemeViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}