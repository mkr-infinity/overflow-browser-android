package com.overflow.browser.browser

import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

/**
 * Bridges WebView features that need an Activity (file chooser, fullscreen
 * video, runtime permissions) without letting the ViewModel retain an Activity.
 */
interface ChromeDelegate {
    fun onShowFileChooser(
        webView: WebView,
        callback: ValueCallback<Array<Uri>>,
        params: WebChromeClient.FileChooserParams
    ): Boolean

    fun onShowCustomView(view: View, callback: WebChromeClient.CustomViewCallback)

    fun onHideCustomView()

    fun onPermissionRequest(request: PermissionRequest)
}
