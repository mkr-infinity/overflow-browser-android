package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class TabStateTest {
    @Test
    fun browserTab_initializesCorrectly() {
        val tab = BrowserTab(title = "Example", url = "https://example.com", isIncognito = false)
        assertEquals("Example", tab.title)
        assertEquals("https://example.com", tab.url)
        assertFalse(tab.isIncognito)
    }
}
