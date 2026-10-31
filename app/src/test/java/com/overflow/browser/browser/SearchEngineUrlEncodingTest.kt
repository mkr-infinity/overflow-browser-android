package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class SearchEngineUrlEncodingTest {
    @Test
    fun searchUrl_encodesSpecialCharactersCorrectly() {
        val url = SearchEngine.GOOGLE.searchUrl("c++ & java", "")
        assertTrue(url.contains("c%2B%2B"))
    }
}
