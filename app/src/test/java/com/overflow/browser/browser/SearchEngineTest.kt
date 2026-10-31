package com.overflow.browser.browser

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchEngineTest {

    @Test
    fun fromId_returnsMatchingEngineOrFallback() {
        assertEquals(SearchEngine.DUCKDUCKGO, SearchEngine.fromId("duckduckgo"))
        assertEquals(SearchEngine.GOOGLE, SearchEngine.fromId("google"))
        assertEquals(SearchEngine.BING, SearchEngine.fromId("bing"))
        assertEquals(SearchEngine.BRAVE, SearchEngine.fromId("brave"))
        assertEquals(SearchEngine.ECOSIA, SearchEngine.fromId("ecosia"))
        assertEquals(SearchEngine.STARTPAGE, SearchEngine.fromId("startpage"))
        assertEquals(SearchEngine.CUSTOM, SearchEngine.fromId("custom"))
        assertEquals(SearchEngine.DUCKDUCKGO, SearchEngine.fromId("invalid-engine"))
    }

    @Test
    fun searchUrl_formatsQueryCorrectly() {
        val ddgUrl = SearchEngine.DUCKDUCKGO.searchUrl("android compose", "")
        assertEquals("https://duckduckgo.com/?q=android+compose", ddgUrl)

        val googleUrl = SearchEngine.GOOGLE.searchUrl("jetpack compose", "")
        assertEquals("https://www.google.com/search?q=jetpack+compose", googleUrl)

        val customUrl = SearchEngine.CUSTOM.searchUrl("privacy", "https://kagi.com/search?q=%s")
        assertEquals("https://kagi.com/search?q=privacy", customUrl)
    }

    @Test
    fun letter_returnsFirstCharacterOfDisplayName() {
        assertEquals("D", SearchEngine.DUCKDUCKGO.letter)
        assertEquals("G", SearchEngine.GOOGLE.letter)
        assertEquals("B", SearchEngine.BING.letter)
    }
}
