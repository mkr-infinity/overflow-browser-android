package com.overflow.browser.data

import java.util.UUID

data class Bookmark(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
)
