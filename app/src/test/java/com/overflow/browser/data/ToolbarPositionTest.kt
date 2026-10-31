package com.overflow.browser.data

import org.junit.Assert.*
import org.junit.Test

class ToolbarPositionTest {
    @Test
    fun toolbarPosition_containsTopAndBottom() {
        assertEquals(ToolbarPosition.TOP, ToolbarPosition.valueOf("TOP"))
        assertEquals(ToolbarPosition.BOTTOM, ToolbarPosition.valueOf("BOTTOM"))
    }
}
