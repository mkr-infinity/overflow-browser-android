package com.overflow.browser

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrowserTab(
    val id: Long,
    val title: String,
    val url: String,
    val isPrivate: Boolean = false,
    val faviconUrl: String? = null
)

data class BrowserUiState(
    val tabs: List<BrowserTab> = listOf(BrowserTab(1L, "Homepage", "about:home")),
    val currentTabIndex: Int = 0,
    val searchQuery: String = "",
    val isPrivateMode: Boolean = false,
    val adBlockEnabled: Boolean = true,
    val trackerProtectionEnabled: Boolean = true,
    val darkMode: Boolean = true
)

class BrowserViewModel(context: Context) : ViewModel() {
    private val appContext = context.applicationContext
    private val prefs = BrowserPreferences(appContext)

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.settings.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    adBlockEnabled = settings.adBlockEnabled,
                    trackerProtectionEnabled = settings.trackerProtectionEnabled,
                    darkMode = settings.darkMode,
                    isPrivateMode = settings.privateMode
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun addTab(url: String = "about:home", private: Boolean = false) {
        val nextId = (_uiState.value.tabs.maxOfOrNull { it.id } ?: 0L) + 1
        val newTab = BrowserTab(id = nextId, title = if (url == "about:home") "Homepage" else url, url = url, isPrivate = private)
        val updatedTabs = _uiState.value.tabs.toMutableList().apply { add(newTab) }
        _uiState.value = _uiState.value.copy(tabs = updatedTabs, currentTabIndex = updatedTabs.lastIndex)
    }

    fun closeTab(index: Int) {
        val tabs = _uiState.value.tabs.toMutableList()
        if (tabs.size <= 1) return
        tabs.removeAt(index)
        val newIndex = (index.coerceAtMost(tabs.lastIndex)).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(tabs = tabs, currentTabIndex = newIndex)
    }

    fun switchTab(index: Int) {
        if (index in _uiState.value.tabs.indices) {
            _uiState.value = _uiState.value.copy(currentTabIndex = index)
        }
    }

    fun updateCurrentTabTitle(title: String) {
        val tabs = _uiState.value.tabs.toMutableList()
        val idx = _uiState.value.currentTabIndex
        if (idx in tabs.indices) {
            tabs[idx] = tabs[idx].copy(title = title)
            _uiState.value = _uiState.value.copy(tabs = tabs)
        }
    }

    fun updateCurrentTabUrl(url: String) {
        val tabs = _uiState.value.tabs.toMutableList()
        val idx = _uiState.value.currentTabIndex
        if (idx in tabs.indices) {
            tabs[idx] = tabs[idx].copy(url = url)
            _uiState.value = _uiState.value.copy(tabs = tabs)
        }
    }

    fun openUrl(rawUrl: String) {
        val normalized = when {
            rawUrl.isBlank() -> "about:home"
            rawUrl.startsWith("http://") || rawUrl.startsWith("https://") -> rawUrl
            rawUrl.contains(".") || rawUrl.contains("/") -> "https://$rawUrl"
            else -> "https://www.google.com/search?q=${rawUrl.replace(" ", "+")}" 
        }
        updateCurrentTabUrl(normalized)
        _uiState.value = _uiState.value.copy(searchQuery = normalized)
    }

    fun togglePrivateMode() {
        _uiState.value = _uiState.value.copy(isPrivateMode = !_uiState.value.isPrivateMode)
        viewModelScope.launch { prefs.setPrivateMode(_uiState.value.isPrivateMode) }
    }

    fun toggleAdBlock() {
        val enabled = !_uiState.value.adBlockEnabled
        _uiState.value = _uiState.value.copy(adBlockEnabled = enabled)
        viewModelScope.launch { prefs.setAdBlockEnabled(enabled) }
    }

    fun toggleTrackerProtection() {
        val enabled = !_uiState.value.trackerProtectionEnabled
        _uiState.value = _uiState.value.copy(trackerProtectionEnabled = enabled)
        viewModelScope.launch { prefs.setTrackerProtectionEnabled(enabled) }
    }

    fun toggleDarkMode() {
        val enabled = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = enabled)
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }
}
