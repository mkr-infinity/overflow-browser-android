package com.overflow.browser.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    @Test
    fun formatBytes_convertsCorrectlyAcrossUnits() {
        assertEquals("—", DateUtils.formatBytes(0L))
        assertEquals("—", DateUtils.formatBytes(-50L))
        assertEquals("512 B", DateUtils.formatBytes(512L))
        assertEquals("2.0 KB", DateUtils.formatBytes(2048L))
        assertEquals("5.0 MB", DateUtils.formatBytes(5 * 1024 * 1024L))
        assertEquals("1.50 GB", DateUtils.formatBytes((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun dayLabel_identifiesTodayAndYesterday() {
        val now = System.currentTimeMillis()
        assertEquals("Today", DateUtils.dayLabel(now))

        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
        assertEquals("Yesterday", DateUtils.dayLabel(yesterday))
    }

    @Test
    fun greeting_returnsAppropriateSalutation() {
        val greeting = DateUtils.greeting()
        assertTrue(
            greeting == "Good morning" ||
                greeting == "Good afternoon" ||
                greeting == "Good evening"
        )
    }
}
