package com.overflow.browser.util

import org.junit.Assert.*
import org.junit.Test

class UrlDomainExtractionTest {
    @Test
    fun host_handlesComplexDomains() {
        assertEquals("sub.domain.org", UrlUtils.host("https://sub.domain.org:8080/path"))
        assertEquals("127.0.0.1", UrlUtils.host("http://127.0.0.1:3000"))
    }
}
