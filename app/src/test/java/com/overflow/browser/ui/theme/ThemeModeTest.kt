package com.overflow.browser.ui.theme

import org.junit.Assert.*
import org.junit.Test

class ThemeModeTest {
    @Test
    fun themeMode_containsExpectedValues() {
        assertTrue(ThemeMode.entries.contains(ThemeMode.SYSTEM))
        assertTrue(ThemeMode.entries.contains(ThemeMode.LIGHT))
        assertTrue(ThemeMode.entries.contains(ThemeMode.DARK))
    }
}
