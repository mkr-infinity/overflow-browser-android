package com.overflow.browser.data

import org.junit.Assert.*
import org.junit.Test

class BookmarkValidationTest {
    @Test
    fun bookmark_preservesTitleAndUrl() {
        val b = Bookmark(title = "Kotlin", url = "https://kotlinlang.org")
        assertEquals("Kotlin", b.title)
        assertEquals("https://kotlinlang.org", b.url)
    }
}
