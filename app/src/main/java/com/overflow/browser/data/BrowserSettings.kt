package com.overflow.browser.data

import com.overflow.browser.browser.SearchEngine
import com.overflow.browser.ui.theme.ThemeMode

enum class ToolbarPosition {
    TOP,
    BOTTOM
}

data class BrowserSettings(
    val searchEngineId: String = SearchEngine.DUCKDUCKGO.id,
    val customSearchUrl: String = "",
    val homepage: String = "overflow://home",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val toolbarPosition: ToolbarPosition = ToolbarPosition.BOTTOM,
    val hideToolbarOnScroll: Boolean = true,
    val adBlockEnabled: Boolean = true,
    val javascriptEnabled: Boolean = true,
    val imagesEnabled: Boolean = true,
    val cookiesEnabled: Boolean = true,
    val httpsOnly: Boolean = false,
    val safeBrowsing: Boolean = true,
    val searchSuggestions: Boolean = true,
    val restoreTabs: Boolean = true,
    val desktopModeDefault: Boolean = false,
    val darkWebPages: Boolean = false,
    val readerFontScale: Float = 1.0f,
    val openLinksInNewTab: Boolean = false
) {
    val searchEngine: SearchEngine
        get() = SearchEngine.fromId(searchEngineId)
}
