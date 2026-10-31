package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class ReaderModeParsingTest {
    @Test
    fun parse_returnsNullOnBlank() {
        assertNull(ReaderMode.parse(""))
        assertNull(ReaderMode.parse(null))
        assertNull(ReaderMode.parse("null"))
    }
}
