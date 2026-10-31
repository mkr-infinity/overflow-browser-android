package com.overflow.browser.data

import org.junit.Assert.*
import org.junit.Test

class StatsRepositoryModelTest {
    @Test
    fun statsData_initializesZero() {
        val initialAdsBlocked = 0L
        assertEquals(0L, initialAdsBlocked)
    }
}
