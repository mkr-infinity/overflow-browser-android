package com.overflow.browser.browser

import com.overflow.browser.data.BrowserSettings

enum class BrowserOverlay {
    NONE,
    TABS,
    BOOKMARKS,
    HISTORY,
    DOWNLOADS,
    SETTINGS
}

data class BrowserUiState(
    val tabs: List<BrowserTab> = emptyList(),
    val activeTabId: String? = null,
    val isReady: Boolean = false,
    val settings: BrowserSettings = BrowserSettings(),
    val overlay: BrowserOverlay = BrowserOverlay.NONE,
    val isMenuOpen: Boolean = false,
    val isSearchOpen: Boolean = false,
    val searchQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val isFindOpen: Boolean = false,
    val findQuery: String = "",
    val findMatches: Int = 0,
    val findCurrent: Int = 0,
    val readerContent: ReaderContent? = null,
    val jsDialog: JsDialogState? = null,
    val snackbarMessage: String? = null,
    val toolbarVisible: Boolean = true,
    val isFullscreen: Boolean = false
) {
    val activeTab: BrowserTab?
        get() = tabs.firstOrNull { it.id == activeTabId }

    val normalTabCount: Int
        get() = tabs.count { !it.isIncognito }

    val privateTabCount: Int
        get() = tabs.count { it.isIncognito }

    val hasPrivateTabs: Boolean
        get() = tabs.any { it.isIncognito }
}
