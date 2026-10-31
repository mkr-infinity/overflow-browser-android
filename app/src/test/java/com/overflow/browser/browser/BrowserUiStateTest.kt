package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class BrowserUiStateTest {
    @Test
    fun browserUiState_hasSensibleDefaults() {
        val state = BrowserUiState()
        assertTrue(state.tabs.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isIncognito)
        assertFalse(state.canGoBack)
        assertFalse(state.canGoForward)
    }
}
