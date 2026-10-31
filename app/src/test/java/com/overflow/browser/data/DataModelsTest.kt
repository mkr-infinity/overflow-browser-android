package com.overflow.browser.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelsTest {

    @Test
    fun bookmark_initializesWithSensibleDefaults() {
        val now = System.currentTimeMillis()
        val bookmark = Bookmark(title = "GitHub", url = "https://github.com")

        assertNotNull(bookmark.id)
        assertTrue(bookmark.id.isNotBlank())
        assertEquals("GitHub", bookmark.title)
        assertEquals("https://github.com", bookmark.url)
        assertTrue(bookmark.createdAt >= now)
    }

    @Test
    fun historyEntry_initializesWithSensibleDefaults() {
        val now = System.currentTimeMillis()
        val entry = HistoryEntry(title = "Kotlin Documentation", url = "https://kotlinlang.org")

        assertNotNull(entry.id)
        assertTrue(entry.id.isNotBlank())
        assertEquals("Kotlin Documentation", entry.title)
        assertEquals("https://kotlinlang.org", entry.url)
        assertTrue(entry.visitedAt >= now)
    }

    @Test
    fun downloadRecord_tracksProgressAndStatus() {
        val record = DownloadRecord(
            fileName = "overflow.apk",
            url = "https://example.com/overflow.apk",
            destinationUri = "content://downloads/1",
            totalBytes = 10485760L,
            downloadedBytes = 5242880L,
            status = DownloadStatus.IN_PROGRESS
        )

        assertEquals("overflow.apk", record.fileName)
        assertEquals(10485760L, record.totalBytes)
        assertEquals(5242880L, record.downloadedBytes)
        assertEquals(DownloadStatus.IN_PROGRESS, record.status)
        assertEquals(50, (record.downloadedBytes * 100 / record.totalBytes).toInt())
    }
}
