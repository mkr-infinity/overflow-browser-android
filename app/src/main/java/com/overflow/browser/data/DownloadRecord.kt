package com.overflow.browser.data

data class DownloadRecord(
    val id: Long,
    val fileName: String,
    val url: String,
    val mimeType: String?,
    val createdAt: Long = System.currentTimeMillis()
)
