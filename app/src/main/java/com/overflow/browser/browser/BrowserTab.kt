package com.overflow.browser.browser

import android.graphics.Bitmap
import com.overflow.browser.util.UrlUtils
import java.util.UUID

data class BrowserTab(
    val id: String = UUID.randomUUID().toString(),
    val url: String = UrlUtils.HOME_URL,
    val title: String = "New tab",
    val favicon: Bitmap? = null,
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isIncognito: Boolean = false,
    val isDesktopMode: Boolean = false,
    val error: String? = null
) {
    val isHome: Boolean
        get() = url == UrlUtils.HOME_URL || url.isBlank()

    val displayTitle: String
        get() = title.ifBlank { UrlUtils.host(url) }

    val displayUrl: String
        get() = UrlUtils.prettyUrl(url)

    val host: String
        get() = UrlUtils.host(url)

    val isSecure: Boolean
        get() = UrlUtils.isSecure(url)
}
