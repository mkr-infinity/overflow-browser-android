package com.overflow.browser.util

import com.overflow.browser.browser.SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlUtilsTest {

    @Test
    fun looksLikeUrl_identifiesWebAddressesCorrectly() {
        assertTrue(UrlUtils.looksLikeUrl("google.com"))
        assertTrue(UrlUtils.looksLikeUrl("https://github.com"))
        assertTrue(UrlUtils.looksLikeUrl("http://example.org/path?q=1"))
        assertTrue(UrlUtils.looksLikeUrl("localhost:8080"))
        assertTrue(UrlUtils.looksLikeUrl("192.168.1.1"))

        assertFalse(UrlUtils.looksLikeUrl(""))
        assertFalse(UrlUtils.looksLikeUrl("   "))
        assertFalse(UrlUtils.looksLikeUrl("search for something"))
        assertFalse(UrlUtils.looksLikeUrl("justaword"))
    }

    @Test
    fun toUrl_handlesEmptyAndSpecialSchemes() {
        assertEquals(UrlUtils.HOME_URL, UrlUtils.toUrl("", SearchEngine.DUCKDUCKGO, "", false))
        assertEquals(UrlUtils.HOME_URL, UrlUtils.toUrl("overflow://home", SearchEngine.DUCKDUCKGO, "", false))
        assertEquals("about:blank", UrlUtils.toUrl("about:blank", SearchEngine.DUCKDUCKGO, "", false))
    }

    @Test
    fun toUrl_upgradesHttpWhenHttpsOnlyEnabled() {
        val result = UrlUtils.toUrl("http://insecure.example.com", SearchEngine.DUCKDUCKGO, "", true)
        assertEquals("https://insecure.example.com", result)
    }

    @Test
    fun toUrl_generatesSearchQueryForPlainKeywords() {
        val result = UrlUtils.toUrl("kotlin coroutines", SearchEngine.DUCKDUCKGO, "", false)
        assertEquals("https://duckduckgo.com/?q=kotlin+coroutines", result)
    }

    @Test
    fun host_stripsWwwAndExtractsDomain() {
        assertEquals("google.com", UrlUtils.host("https://www.google.com/search?q=test"))
        assertEquals("github.com", UrlUtils.host("https://github.com/mkr-infinity"))
        assertEquals("Overflow", UrlUtils.host(UrlUtils.HOME_URL))
    }

    @Test
    fun prettyUrl_formatsForDisplay() {
        assertEquals("github.com/mkr-infinity", UrlUtils.prettyUrl("https://www.github.com/mkr-infinity/"))
        assertEquals("Start page", UrlUtils.prettyUrl(UrlUtils.HOME_URL))
        assertEquals("Blank page", UrlUtils.prettyUrl(UrlUtils.BLANK_URL))
    }

    @Test
    fun faviconLetter_extractsFirstAlphaCharacter() {
        assertEquals("G", UrlUtils.faviconLetter("https://github.com"))
        assertEquals("A", UrlUtils.faviconLetter("https://www.android.com"))
        assertEquals("O", UrlUtils.faviconLetter(UrlUtils.HOME_URL))
    }
}
