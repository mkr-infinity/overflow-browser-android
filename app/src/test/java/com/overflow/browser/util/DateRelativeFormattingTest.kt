package com.overflow.browser.util

import org.junit.Assert.*
import org.junit.Test

class DateRelativeFormattingTest {
    @Test
    fun formatTime_formatsNonEmptyString() {
        val timeStr = DateUtils.formatTime(System.currentTimeMillis())
        assertTrue(timeStr.isNotBlank())
    }
}
