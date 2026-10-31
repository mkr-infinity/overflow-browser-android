package com.overflow.browser.browser

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil

data class DownloadInfo(
    val id: Long,
    val fileName: String,
    val status: Int,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val mimeType: String?,
    val localUri: Uri?
) {
    val isComplete: Boolean
        get() = status == DownloadManager.STATUS_SUCCESSFUL

    val isRunning: Boolean
        get() = status == DownloadManager.STATUS_RUNNING || status == DownloadManager.STATUS_PENDING

    val progress: Float
        get() = if (totalBytes > 0L) {
            (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }
}

class DownloadHandler(private val context: Context) {

    private val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    fun enqueue(
        url: String,
        userAgent: String?,
        contentDisposition: String?,
        mimeType: String?
    ): Long? {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return null
        val scheme = uri.scheme ?: return null
        if (scheme != "http" && scheme != "https") return null

        val fileName = URLUtil.guessFileName(url, contentDisposition, mimeType)
        val request = DownloadManager.Request(uri).apply {
            setTitle(fileName)
            setDescription("Downloading with Overflow Browser")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
            mimeType?.takeIf { it.isNotBlank() }?.let { setMimeType(it) }
            if (!userAgent.isNullOrBlank()) addRequestHeader("User-Agent", userAgent)
            CookieManager.getInstance().getCookie(url)?.let { addRequestHeader("Cookie", it) }

            runCatching {
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            }.onFailure {
                setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)
            }
        }

        return runCatching { manager.enqueue(request) }.getOrNull()
    }

    fun query(id: Long): DownloadInfo? {
        val cursor = manager.query(DownloadManager.Query().setFilterById(id)) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val fileName = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TITLE))
                ?: "Download"
            val downloaded = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val total = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )
            val mime = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_MEDIA_TYPE))
            val localUri = if (status == DownloadManager.STATUS_SUCCESSFUL) {
                manager.getUriForDownloadedFile(id)
            } else {
                null
            }
            return DownloadInfo(id, fileName, status, downloaded, total, mime, localUri)
        }
    }

    fun open(id: Long): Intent? {
        val info = query(id) ?: return null
        val uri = info.localUri ?: return null
        return Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, info.mimeType ?: "*/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun share(id: Long): Intent? {
        val info = query(id) ?: return null
        val uri = info.localUri ?: return null
        val intent = Intent(Intent.ACTION_SEND)
            .setType(info.mimeType ?: "*/*")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        return Intent.createChooser(intent, "Share download")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun remove(id: Long) {
        manager.remove(id)
    }
}
