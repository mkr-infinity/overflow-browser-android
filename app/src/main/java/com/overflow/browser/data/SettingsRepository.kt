package com.overflow.browser.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.overflow.browser.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "overflow_settings"
)

class SettingsRepository(context: Context) {

    private val dataStore = context.settingsDataStore

    val settings: Flow<BrowserSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map { prefs ->
            BrowserSettings(
                searchEngineId = prefs[Keys.SEARCH_ENGINE] ?: BrowserSettings().searchEngineId,
                customSearchUrl = prefs[Keys.CUSTOM_SEARCH_URL] ?: "",
                homepage = prefs[Keys.HOMEPAGE] ?: BrowserSettings().homepage,
                themeMode = prefs[Keys.THEME_MODE].toThemeMode(),
                dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
                toolbarPosition = prefs[Keys.TOOLBAR_POSITION].toToolbarPosition(),
                hideToolbarOnScroll = prefs[Keys.HIDE_TOOLBAR] ?: true,
                adBlockEnabled = prefs[Keys.AD_BLOCK] ?: true,
                javascriptEnabled = prefs[Keys.JAVASCRIPT] ?: true,
                imagesEnabled = prefs[Keys.IMAGES] ?: true,
                cookiesEnabled = prefs[Keys.COOKIES] ?: true,
                httpsOnly = prefs[Keys.HTTPS_ONLY] ?: false,
                safeBrowsing = prefs[Keys.SAFE_BROWSING] ?: true,
                searchSuggestions = prefs[Keys.SEARCH_SUGGESTIONS] ?: true,
                restoreTabs = prefs[Keys.RESTORE_TABS] ?: true,
                desktopModeDefault = prefs[Keys.DESKTOP_DEFAULT] ?: false,
                darkWebPages = prefs[Keys.DARK_WEB_PAGES] ?: false,
                readerFontScale = prefs[Keys.READER_FONT_SCALE] ?: 1.0f,
                openLinksInNewTab = prefs[Keys.OPEN_LINKS_NEW_TAB] ?: false
            )
        }

    suspend fun setSearchEngineId(value: String) = put(Keys.SEARCH_ENGINE, value)
    suspend fun setCustomSearchUrl(value: String) = put(Keys.CUSTOM_SEARCH_URL, value)
    suspend fun setHomepage(value: String) = put(Keys.HOMEPAGE, value)
    suspend fun setThemeMode(value: ThemeMode) = put(Keys.THEME_MODE, value.name)
    suspend fun setDynamicColor(value: Boolean) = put(Keys.DYNAMIC_COLOR, value)
    suspend fun setToolbarPosition(value: ToolbarPosition) = put(Keys.TOOLBAR_POSITION, value.name)
    suspend fun setHideToolbarOnScroll(value: Boolean) = put(Keys.HIDE_TOOLBAR, value)
    suspend fun setAdBlockEnabled(value: Boolean) = put(Keys.AD_BLOCK, value)
    suspend fun setJavascriptEnabled(value: Boolean) = put(Keys.JAVASCRIPT, value)
    suspend fun setImagesEnabled(value: Boolean) = put(Keys.IMAGES, value)
    suspend fun setCookiesEnabled(value: Boolean) = put(Keys.COOKIES, value)
    suspend fun setHttpsOnly(value: Boolean) = put(Keys.HTTPS_ONLY, value)
    suspend fun setSafeBrowsing(value: Boolean) = put(Keys.SAFE_BROWSING, value)
    suspend fun setSearchSuggestions(value: Boolean) = put(Keys.SEARCH_SUGGESTIONS, value)
    suspend fun setRestoreTabs(value: Boolean) = put(Keys.RESTORE_TABS, value)
    suspend fun setDesktopModeDefault(value: Boolean) = put(Keys.DESKTOP_DEFAULT, value)
    suspend fun setDarkWebPages(value: Boolean) = put(Keys.DARK_WEB_PAGES, value)
    suspend fun setReaderFontScale(value: Float) = put(Keys.READER_FONT_SCALE, value)
    suspend fun setOpenLinksInNewTab(value: Boolean) = put(Keys.OPEN_LINKS_NEW_TAB, value)

    private suspend fun <T> put(key: Preferences.Key<T>, value: T) {
        dataStore.edit { prefs -> prefs[key] = value }
    }

    private fun String?.toThemeMode(): ThemeMode =
        runCatching { ThemeMode.valueOf(this ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)

    private fun String?.toToolbarPosition(): ToolbarPosition =
        runCatching { ToolbarPosition.valueOf(this ?: ToolbarPosition.BOTTOM.name) }
            .getOrDefault(ToolbarPosition.BOTTOM)

    private object Keys {
        val SEARCH_ENGINE = stringPreferencesKey("search_engine")
        val CUSTOM_SEARCH_URL = stringPreferencesKey("custom_search_url")
        val HOMEPAGE = stringPreferencesKey("homepage")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val TOOLBAR_POSITION = stringPreferencesKey("toolbar_position")
        val HIDE_TOOLBAR = booleanPreferencesKey("hide_toolbar_on_scroll")
        val AD_BLOCK = booleanPreferencesKey("ad_block")
        val JAVASCRIPT = booleanPreferencesKey("javascript")
        val IMAGES = booleanPreferencesKey("images")
        val COOKIES = booleanPreferencesKey("cookies")
        val HTTPS_ONLY = booleanPreferencesKey("https_only")
        val SAFE_BROWSING = booleanPreferencesKey("safe_browsing")
        val SEARCH_SUGGESTIONS = booleanPreferencesKey("search_suggestions")
        val RESTORE_TABS = booleanPreferencesKey("restore_tabs")
        val DESKTOP_DEFAULT = booleanPreferencesKey("desktop_default")
        val DARK_WEB_PAGES = booleanPreferencesKey("dark_web_pages")
        val READER_FONT_SCALE = floatPreferencesKey("reader_font_scale")
        val OPEN_LINKS_NEW_TAB = booleanPreferencesKey("open_links_new_tab")
    }
}
