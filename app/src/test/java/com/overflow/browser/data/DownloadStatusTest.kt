package com.overflow.browser.data

import org.junit.Assert.*
import org.junit.Test

class DownloadStatusTest {
    @Test
    fun downloadStatus_containsExpectedStates() {
        assertTrue(DownloadStatus.entries.contains(DownloadStatus.PENDING))
        assertTrue(DownloadStatus.entries.contains(DownloadStatus.IN_PROGRESS))
        assertTrue(DownloadStatus.entries.contains(DownloadStatus.COMPLETED))
        assertTrue(DownloadStatus.entries.contains(DownloadStatus.FAILED))
        assertTrue(DownloadStatus.entries.contains(DownloadStatus.CANCELLED))
    }
}
