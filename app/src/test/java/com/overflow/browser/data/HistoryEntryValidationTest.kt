package com.overflow.browser.data

import org.junit.Assert.*
import org.junit.Test

class HistoryEntryValidationTest {
    @Test
    fun historyEntry_hasValidVisitedAt() {
        val h = HistoryEntry(title = "Android", url = "https://android.com")
        assertTrue(h.visitedAt > 0L)
    }
}
