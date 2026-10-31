package com.overflow.browser

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.browserDataStore: DataStore<Preferences> by preferencesDataStore(name = "browser_settings")

data class BrowserSettings(
    val adBlockEnabled: Boolean = true,
    val trackerProtectionEnabled: Boolean = true,
    val darkMode: Boolean = true,
    val privateMode: Boolean = false
)

class BrowserPreferences(private val context: Context) {
    private val AD_BLOCK = booleanPreferencesKey("ad_block")
    private val TRACKER_PROTECTION = booleanPreferencesKey("tracker_protection")
    private val DARK_MODE = booleanPreferencesKey("dark_mode")
    private val PRIVATE_MODE = booleanPreferencesKey("private_mode")

    val settings: Flow<BrowserSettings> = context.browserDataStore.data.map { prefs ->
        BrowserSettings(
            adBlockEnabled = prefs[AD_BLOCK] ?: true,
            trackerProtectionEnabled = prefs[TRACKER_PROTECTION] ?: true,
            darkMode = prefs[DARK_MODE] ?: true,
            privateMode = prefs[PRIVATE_MODE] ?: false
        )
    }

    suspend fun setAdBlockEnabled(enabled: Boolean) {
        context.browserDataStore.edit { it[AD_BLOCK] = enabled }
    }

    suspend fun setTrackerProtectionEnabled(enabled: Boolean) {
        context.browserDataStore.edit { it[TRACKER_PROTECTION] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.browserDataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setPrivateMode(enabled: Boolean) {
        context.browserDataStore.edit { it[PRIVATE_MODE] = enabled }
    }
}
