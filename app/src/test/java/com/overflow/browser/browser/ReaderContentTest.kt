package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class ReaderContentTest {
    @Test
    fun readerContent_storesExtractedData() {
        val content = ReaderContent(title = "Test", byline = "Author", text = "Body text", url = "https://example.com")
        assertEquals("Test", content.title)
        assertEquals("Author", content.byline)
        assertEquals("Body text", content.text)
    }
}
