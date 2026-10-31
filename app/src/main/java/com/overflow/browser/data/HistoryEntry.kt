package com.overflow.browser.data

import java.util.UUID

data class HistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)
