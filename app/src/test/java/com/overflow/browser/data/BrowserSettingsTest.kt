package com.overflow.browser.data

import com.overflow.browser.browser.SearchEngine
import com.overflow.browser.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserSettingsTest {

    @Test
    fun defaultSettings_haveSensiblePrivacyAndSecurityDefaults() {
        val settings = BrowserSettings()

        assertEquals(SearchEngine.DUCKDUCKGO.id, settings.searchEngineId)
        assertEquals(SearchEngine.DUCKDUCKGO, settings.searchEngine)
        assertEquals(ToolbarPosition.BOTTOM, settings.toolbarPosition)
        assertEquals(ThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.adBlockEnabled)
        assertTrue(settings.javascriptEnabled)
        assertTrue(settings.imagesEnabled)
        assertTrue(settings.cookiesEnabled)
        assertTrue(settings.safeBrowsing)
        assertTrue(settings.searchSuggestions)
        assertTrue(settings.restoreTabs)
        assertFalse(settings.httpsOnly)
        assertFalse(settings.desktopModeDefault)
        assertFalse(settings.darkWebPages)
    }

    @Test
    fun searchEngineProperty_resolvesConfiguredId() {
        val settings = BrowserSettings(searchEngineId = SearchEngine.GOOGLE.id)
        assertEquals(SearchEngine.GOOGLE, settings.searchEngine)
    }

    @Test
    fun copySettings_preservesUnchangedFields() {
        val original = BrowserSettings()
        val updated = original.copy(
            toolbarPosition = ToolbarPosition.TOP,
            httpsOnly = true,
            themeMode = ThemeMode.DARK
        )

        assertEquals(ToolbarPosition.TOP, updated.toolbarPosition)
        assertTrue(updated.httpsOnly)
        assertEquals(ThemeMode.DARK, updated.themeMode)
        assertTrue(updated.adBlockEnabled)
        assertEquals(original.homepage, updated.homepage)
    }
}
