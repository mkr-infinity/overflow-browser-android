package com.overflow.browser.browser

import org.junit.Assert.*
import org.junit.Test

class JsDialogStateTest {
    @Test
    fun jsDialogState_alertStoresMessage() {
        val alert = JsDialogState.Alert("Test message")
        assertEquals("Test message", alert.message)
    }
}
