package com.overflow.browser.browser

import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.ClientCertRequest
import android.webkit.HttpAuthHandler
import android.webkit.RenderProcessGoneDetail
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.overflow.browser.data.BrowserSettings
import java.io.ByteArrayInputStream
import java.util.Locale

class OverflowWebViewClient(
    private val tabId: String,
    private val settingsProvider: () -> BrowserSettings,
    private val adBlocker: AdBlocker,
    private val onPageStarted: (String, String) -> Unit,
    private val onPageFinished: (String, String) -> Unit,
    private val onUrlChanged: (String, String) -> Unit,
    private val onError: (String, String) -> Unit,
    private val onBlocked: (Int) -> Unit,
    private val onExternalUrl: (Uri) -> Unit,
    private val onRendererGone: (String) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val webView = view ?: return false
        val uri = request?.url ?: return false
        val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: return false

        return when (scheme) {
            "http", "https" -> {
                if (scheme == "http" &&
                    settingsProvider().httpsOnly &&
                    request.method.equals("GET", ignoreCase = true)
                ) {
                    val upgraded = uri.buildUpon().scheme("https").build()
                    webView.loadUrl(upgraded.toString())
                    true
                } else {
                    false
                }
            }
            "about", "data", "file", "content", "blob", "javascript" -> false
            else -> {
                onExternalUrl(uri)
                true
            }
        }
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?
    ): WebResourceResponse? {
        val uri = request?.url
        if (uri != null && settingsProvider().adBlockEnabled && adBlocker.shouldBlock(uri)) {
            onBlocked(1)
            return WebResourceResponse(
                "text/plain",
                "utf-8",
                ByteArrayInputStream(ByteArray(0))
            )
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        if (url != null) onPageStarted(tabId, url)
        super.onPageStarted(view, url, favicon)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        if (url != null) onPageFinished(tabId, url)
        super.onPageFinished(view, url)
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        if (url != null) onUrlChanged(tabId, url)
        super.doUpdateVisitedHistory(view, url, isReload)
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        if (request?.isForMainFrame == true) {
            onError(tabId, error?.description?.toString() ?: "The page could not be loaded")
        }
        super.onReceivedError(view, request, error)
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        handler?.cancel()
        onError(tabId, "This site's security certificate is not trusted")
    }

    override fun onReceivedClientCertRequest(view: WebView?, request: ClientCertRequest?) {
        request?.cancel()
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        handler?.cancel()
    }

    override fun onRenderProcessGone(
        view: WebView?,
        detail: RenderProcessGoneDetail?
    ): Boolean {
        onRendererGone(tabId)
        return true
    }
}
