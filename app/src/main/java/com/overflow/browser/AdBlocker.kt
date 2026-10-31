package com.overflow.browser

import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.WorkerThread
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

object AdBlocker {
    private val blockedDomains = setOf(
        "doubleclick.net",
        "googlesyndication.com",
        "google-analytics.com",
        "adservice.google.com",
        "adsystem.com",
        "facebook.com/tr",
        "analytics.yahoo.com",
        "segment.com",
        "moatads.com",
        "scorecardresearch.com"
    )

    private val blockedPaths = setOf(
        "/ads/",
        "/analytics/",
        "/tracking/",
        "/advertising/",
        "/beacon/",
        "/metrics/"
    )

    fun shouldBlock(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val lower = url.lowercase()
        return blockedDomains.any { lower.contains(it) } || blockedPaths.any { lower.contains(it) }
    }

    fun buildBlockResponse(): WebResourceResponse {
        val empty = "<!doctype html><html><body></body></html>".toByteArray(StandardCharsets.UTF_8)
        return WebResourceResponse(
            "text/html",
            "utf-8",
            ByteArrayInputStream(empty)
        )
    }
}

class OverflowWebViewClient(
    private val onPageStarted: (String) -> Unit,
    private val onPageFinished: (String) -> Unit,
    private val shouldBlockRequests: (String) -> Boolean = { false }
) : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
        return if (shouldBlockRequests(url)) {
            AdBlocker.buildBlockResponse()
        } else {
            super.shouldInterceptRequest(view, request)
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (!url.isNullOrBlank()) onPageStarted(url)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        if (!url.isNullOrBlank()) onPageFinished(url)
    }
}
